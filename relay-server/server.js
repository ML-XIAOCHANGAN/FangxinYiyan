const http = require("http");
const { WebSocketServer } = require("ws");

const PORT = Number(process.env.PORT || 8787);

// 每个配对码就是一个房间，房间里最多一位老人、一位守护人。
// 服务端只做转发，不落盘：警报和画面过一遍就丢。
const rooms = new Map();

const server = http.createServer((req, res) => {
  if (req.url === "/health") {
    res.writeHead(200, { "content-type": "application/json" });
    res.end(JSON.stringify({ ok: true, rooms: summarize() }));
    return;
  }
  res.writeHead(404);
  res.end();
});

const wss = new WebSocketServer({ server });

function room(code) {
  if (!rooms.has(code)) rooms.set(code, { elder: null, guardian: null });
  return rooms.get(code);
}

function online(code) {
  const r = rooms.get(code);
  return { elder: Boolean(r && r.elder), guardian: Boolean(r && r.guardian) };
}

function summarize() {
  const out = {};
  for (const [code, r] of rooms) {
    out[code] = { elder: Boolean(r.elder), guardian: Boolean(r.guardian) };
  }
  return out;
}

function send(ws, payload) {
  if (ws && ws.readyState === ws.OPEN) ws.send(JSON.stringify(payload));
}

function peer(ws, slot) {
  const r = rooms.get(ws.room);
  if (!r) return null;
  return slot === "elder" ? r.guardian : r.elder;
}

function broadcastPresence(code) {
  const r = rooms.get(code);
  if (!r) return;
  const msg = { type: "presence", ...online(code) };
  send(r.elder, msg);
  send(r.guardian, msg);
}

function drop(ws) {
  if (!ws.room) return;
  const r = rooms.get(ws.room);
  if (!r) return;
  if (r[ws.role] === ws) r[ws.role] = null;
  broadcastPresence(ws.room);
  if (!r.elder && !r.guardian) rooms.delete(ws.room);
}

wss.on("connection", (ws) => {
  ws.isAlive = true;
  ws.on("pong", () => {
    ws.isAlive = true;
  });

  ws.on("message", (raw) => {
    let msg;
    try {
      msg = JSON.parse(raw.toString());
    } catch {
      return;
    }
    if (msg.type === "join") {
      const code = String(msg.room || "").trim();
      const role = msg.role === "guardian" ? "guardian" : "elder";
      if (!/^\d{6}$/.test(code)) {
        send(ws, { type: "error", message: "配对码要是 6 位数字" });
        return;
      }
      if (ws.room) drop(ws);
      const r = room(code);
      if (r[role]) {
        try {
          r[role].close(4001, "replaced by new connection");
        } catch {}
      }
      r[role] = ws;
      ws.room = code;
      ws.role = role;
      send(ws, { type: "joined", room: code, role });
      console.log(`[join] ${code} ${role} (elder=${Boolean(r.elder)}, guardian=${Boolean(r.guardian)})`);
      broadcastPresence(code);
      return;
    }

    if (!ws.room) {
      send(ws, { type: "error", message: "先 join 再发消息" });
      return;
    }

    // 老人在线则转给守护人，守护人在线则转给老人。
    const target = peer(ws, ws.role);
    if (!target) return;

    if (msg.type === "alert") {
      console.log(`[alert] ${ws.room} ${msg.alert && msg.alert.risk} ${msg.alert && msg.alert.label}`);
      send(target, msg);
    } else if (msg.type === "screen") {
      console.log(`[screen] ${ws.room} on=${msg.on}`);
      send(target, msg);
    } else if (msg.type === "frame") {
      send(target, msg);
    } else if (msg.type === "screen_request") {
      console.log(`[screen_request] ${ws.room}`);
      send(target, msg);
    }
  });

  ws.on("close", () => drop(ws));
  ws.on("error", () => drop(ws));
});

setInterval(() => {
  for (const ws of wss.clients) {
    if (ws.isAlive === false) {
      try {
        ws.terminate();
      } catch {}
      continue;
    }
    ws.isAlive = false;
    try {
      ws.ping();
    } catch {}
  }
}, 30000);

server.listen(PORT, "0.0.0.0", () => {
  console.log(`放心一眼中继已启动：ws://0.0.0.0:${PORT}`);
});
