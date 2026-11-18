package com.fangxin.yiyan

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fangxin.yiyan.analytics.Analytics
import com.fangxin.yiyan.assistant.AssistantClient
import com.fangxin.yiyan.assistant.OwlAnswer
import com.fangxin.yiyan.assistant.OwlException
import com.fangxin.yiyan.assistant.OwlMood
import com.fangxin.yiyan.assistant.OwlState
import com.fangxin.yiyan.data.AppRepository
import com.fangxin.yiyan.domain.AlertEvent
import com.fangxin.yiyan.domain.CheckResult
import com.fangxin.yiyan.domain.CheckSource
import com.fangxin.yiyan.domain.HistoryItem
import com.fangxin.yiyan.domain.RiskCheck
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.domain.SampleCase
import com.fangxin.yiyan.domain.Samples
import com.fangxin.yiyan.domain.TextMasking
import com.fangxin.yiyan.domain.UserRole
import com.fangxin.yiyan.link.LinkClient
import com.fangxin.yiyan.link.LinkEvent
import com.fangxin.yiyan.link.LinkStatus
import com.fangxin.yiyan.ocr.OcrEngine
import com.fangxin.yiyan.service.FloatingMascotService
import com.fangxin.yiyan.service.GuardianLinkService
import com.fangxin.yiyan.service.MascotMood
import com.fangxin.yiyan.service.ScreenShareService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FangxinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository.get(application)
    private var ocrEngine: OcrEngine? = null

    private val _currentResult = MutableStateFlow<CheckResult?>(null)
    val currentResult: StateFlow<CheckResult?> = _currentResult.asStateFlow()

    private val _processing = MutableStateFlow(false)
    val processing: StateFlow<Boolean> = _processing.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _currentAlert = MutableStateFlow<AlertEvent?>(null)
    val currentAlert: StateFlow<AlertEvent?> = _currentAlert.asStateFlow()

    private val _guardianFrame = MutableStateFlow<ImageBitmap?>(null)
    val guardianFrame: StateFlow<ImageBitmap?> = _guardianFrame.asStateFlow()

    private val _guardianScreenOn = MutableStateFlow(false)
    val guardianScreenOn: StateFlow<Boolean> = _guardianScreenOn.asStateFlow()

    private val _screenRequested = MutableStateFlow(false)
    val screenRequested: StateFlow<Boolean> = _screenRequested.asStateFlow()

    private val _owl = MutableStateFlow(OwlState())
    val owl: StateFlow<OwlState> = _owl.asStateFlow()

    val history: StateFlow<List<HistoryItem>> = repository.history
    val settings = repository.settings
    val guardianAlerts: StateFlow<List<AlertEvent>> = repository.guardianAlerts
    val linkStatus: StateFlow<LinkStatus> = LinkClient.status
    val elderScreenSharing: StateFlow<Boolean> = LinkClient.screenSharing

    init {
        val current = settings.value
        Analytics.appOpen(application, current.role?.name?.lowercase())
        if (current.role == UserRole.GUARDIAN && current.guardianPaired) {
            GuardianLinkService.start(application)
        }
        startElderLink()
        viewModelScope.launch(Dispatchers.Default) {
            LinkClient.events.collect { event -> handleLinkEvent(event) }
        }
        viewModelScope.launch {
            LinkClient.status.collect { status ->
                if (!status.peerOnline) {
                    _guardianScreenOn.value = false
                    _guardianFrame.value = null
                }
            }
        }
    }

    fun analyzeText(raw: String, source: CheckSource, onDone: () -> Unit) {
        val text = raw.trim()
        if (text.isEmpty()) {
            _messages.tryEmit("先贴一段字，或者拍张照片。")
            return
        }
        val started = System.currentTimeMillis()
        finish(RiskCheck.analyzeText(text, source), onDone, started)
    }

    fun analyzeBitmap(bitmap: Bitmap, source: CheckSource, onDone: () -> Unit) {
        val started = System.currentTimeMillis()
        _processing.value = true
        val engine = ocrEngine ?: OcrEngine().also { ocrEngine = it }
        engine.recognize(
            bitmap = bitmap,
            onSuccess = { text, confidence ->
                val result = if (text.isBlank()) {
                    _messages.tryEmit("没认出字，换一张清楚点的。")
                    RiskCheck.fallback(source)
                } else {
                    RiskCheck.analyzeText(text, source, ocrConfidence = confidence, ocrText = text)
                }
                recycle(bitmap)
                finish(result, onDone, started)
            },
            onFailure = {
                recycle(bitmap)
                _messages.tryEmit("没看清，先别急着办。")
                finish(RiskCheck.fallback(source), onDone, started)
            },
        )
    }

    fun openSample(sample: SampleCase, onDone: () -> Unit) {
        val result = Samples.toResult(sample)
        _currentResult.value = result
        Analytics.check(getApplication(), result, null)
        onDone()
    }

    fun trackGuide(guideId: String) {
        Analytics.guideView(getApplication(), guideId)
    }

    fun openHistory(item: HistoryItem) {
        _currentResult.value = item.toResult()
    }

    fun setLargeText(enabled: Boolean) {
        repository.setLargeText(enabled)
    }

    fun setFloating(enabled: Boolean) {
        repository.setFloating(enabled)
        if (enabled) {
            FloatingMascotService.startIfAllowed(getApplication())
            _messages.tryEmit("好了。退出软件后，猫头鹰会待在桌面上。")
        } else {
            FloatingMascotService.stop(getApplication())
            _messages.tryEmit("桌面小助手收起来了。")
        }
    }

    fun saveFamilyPhone(phone: String) {
        repository.setFamilyPhone(phone)
        val saved = phone.filter { it.isDigit() }
        _messages.tryEmit(if (saved.isEmpty()) "家人的号码不存了。" else "家人的号码存好了。")
    }

    fun clearHistory() {
        repository.clearHistory()
        _messages.tryEmit("本机记录已清空。")
    }

    fun showMessage(text: String) {
        _messages.tryEmit(text)
    }

    fun setRole(role: UserRole) {
        repository.setRole(role)
        _guardianFrame.value = null
        _guardianScreenOn.value = false
        _currentAlert.value = null
        _screenRequested.value = false
        if (role == UserRole.ELDER) {
            GuardianLinkService.stop(getApplication())
            startElderLink()
        } else {
            LinkClient.disconnect()
            ScreenShareService.stop(getApplication())
            if (repository.settings.value.guardianPaired) {
                GuardianLinkService.start(getApplication())
            }
        }
    }

    fun regeneratePairingCode() {
        val code = repository.regeneratePairingCode()
        startElderLink()
        _messages.tryEmit("新配对码 $code，家人那边要重新填。")
    }

    fun pairGuardian(raw: String) {
        val code = raw.filter { it.isDigit() }
        if (code.length != 6 && code.length != 9) {
            _messages.tryEmit("要填 6 位或 9 位数字，照老人手机上的配对码念。")
            return
        }
        repository.setGuardianRoom(code, paired = true)
        GuardianLinkService.start(getApplication())
        _messages.tryEmit("配对码存好了，正在连老人手机。")
    }

    fun unpairGuardian() {
        GuardianLinkService.stop(getApplication())
        LinkClient.disconnect()
        repository.setGuardianRoom("", paired = false)
        _guardianFrame.value = null
        _guardianScreenOn.value = false
        _messages.tryEmit("配对已解除。")
    }

    fun openGuardianAlert(alert: AlertEvent) {
        _currentAlert.value = alert
    }

    fun clearGuardianAlerts() {
        repository.clearGuardianAlerts()
        _messages.tryEmit("警报记录已清空。")
    }

    fun requestGuardianScreen() {
        val sent = LinkClient.requestScreen()
        _messages.tryEmit(if (sent) "已经提醒老人打开屏幕。" else "还没连上老人手机。")
    }

    fun dismissScreenRequest() {
        _screenRequested.value = false
    }

    fun askOwl(raw: String) {
        if (_owl.value.busy) return
        val text = raw.trim()
        if (text.length < 4) {
            _messages.tryEmit("至少写四个字，小望才看得懂。")
            return
        }
        _owl.value = OwlState(mood = OwlMood.THINKING, busy = true)
        viewModelScope.launch {
            try {
                val answer = AssistantClient.ask(text)
                val mood = when (answer) {
                    is OwlAnswer.Chat -> OwlMood.CHAT
                    is OwlAnswer.Judge ->
                        if (answer.verdict == RiskDecision.RED) OwlMood.RED else OwlMood.AMBER
                }
                _owl.value = OwlState(mood = mood, answer = answer)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: OwlException) {
                _owl.value = OwlState(mood = OwlMood.ERROR)
                _messages.tryEmit(error.userMessage)
            } catch (error: Exception) {
                _owl.value = OwlState(mood = OwlMood.ERROR)
                _messages.tryEmit("小望没连上，等会儿再试。")
            }
        }
    }

    fun resetOwl() {
        if (_owl.value.busy) return
        _owl.value = OwlState()
    }

    private fun startElderLink() {
        val current = repository.settings.value
        if (current.role != UserRole.ELDER) return
        LinkClient.connect(current.relayUrl, current.pairingCode, UserRole.ELDER)
    }

    private fun handleLinkEvent(event: LinkEvent) {
        when (event) {
            is LinkEvent.Frame -> {
                val bitmap = runCatching {
                    BitmapFactory.decodeByteArray(event.jpeg, 0, event.jpeg.size)
                }.getOrNull() ?: return
                _guardianFrame.value = bitmap.asImageBitmap()
            }
            is LinkEvent.ScreenSharing -> {
                _guardianScreenOn.value = event.on
                if (!event.on) _guardianFrame.value = null
            }
            LinkEvent.ScreenRequested -> {
                if (repository.settings.value.role == UserRole.ELDER) {
                    _screenRequested.value = true
                }
            }
            is LinkEvent.Alert -> {
                repository.addGuardianAlert(event.alert)
                FloatingMascotService.setMood(getApplication(), MascotMood.ALERT)
            }
        }
    }

    private fun finish(result: CheckResult, onDone: () -> Unit, startedAt: Long? = null) {
        _currentResult.value = result
        repository.addHistory(result)
        _processing.value = false
        Analytics.check(getApplication(), result, startedAt?.let { System.currentTimeMillis() - it })
        FloatingMascotService.setMood(
            getApplication(),
            when (result.risk) {
                RiskDecision.RED, RiskDecision.AMBER -> MascotMood.ALERT
                RiskDecision.GREEN -> MascotMood.SAFE
                RiskDecision.UNKNOWN -> MascotMood.IDLE
            },
        )
        sendAlertIfNeeded(result)
        onDone()
    }

    private fun sendAlertIfNeeded(result: CheckResult) {
        if (repository.settings.value.role != UserRole.ELDER) return
        if (result.risk == RiskDecision.GREEN) return
        if (result.source == CheckSource.SAMPLE) return
        val alert = AlertEvent(
            id = result.id,
            risk = result.risk,
            label = result.label,
            plain = result.plain,
            maskedText = result.ocrText?.takeIf { it.isNotBlank() }
                ?.let(TextMasking::maskSensitive)
                .orEmpty(),
            actions = result.actions,
            source = result.source,
            createdAt = result.createdAt,
        )
        if (LinkClient.sendAlert(alert)) {
            _messages.tryEmit("这条已经告诉家人了。")
        }
    }

    private fun recycle(bitmap: Bitmap) {
        runCatching { bitmap.recycle() }
    }

    override fun onCleared() {
        ocrEngine?.close()
        ocrEngine = null
        super.onCleared()
    }
}
