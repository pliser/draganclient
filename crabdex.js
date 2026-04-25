#!/usr/bin/env node

const { spawn } = require("child_process");
const readline = require("readline");

const CODEX_BIN = process.env.CODEX_JS_PATH
  || "C:\\Users\\user\\AppData\\Roaming\\npm\\node_modules\\@openai\\codex\\bin\\codex.js";

const forwardedArgs = process.argv.slice(2);

function ansi(code) {
  return `\u001b[${code}m`;
}

const c = {
  reset: ansi(0),
  bold: ansi(1),
  dim: ansi(2),
  green: ansi(32),
  cyan: ansi(36),
  yellow: ansi(33),
  red: ansi(31),
  gray: ansi(90),
  black: ansi(30),
  orangeBg: ansi("48;2;217;119;87"),
  pinkBg: ansi("48;2;255;153;199"),
  whiteBg: ansi("48;2;255;255;255"),
  darkBg: ansi("48;2;47;47;56"),
  mintBg: ansi("48;2;60;214;148"),
  blueBg: ansi("48;2;61;69;211"),
};

const state = {
  input: "",
  running: false,
  child: null,
  blink: false,
  tick: 0,
  mode: "idle",
  promptLines: 0,
};

function cell(bg, text = "  ") {
  return `${bg}${text}${c.reset}`;
}

function empty() {
  return "  ";
}

function crabSprite(mode, blink) {
  const eye = blink ? cell(c.orangeBg) : cell(c.darkBg);
  const mouth = cell(c.whiteBg);
  const body = cell(c.orangeBg);
  const leg = cell(c.orangeBg);
  const blank = empty();

  const sprite = [
    [blank, blank, body, body, body, body, blank, blank],
    [blank, blank, body, eye, body, eye, blank, blank],
    [body, body, body, body, mouth, body, body, blank],
    [blank, leg, blank, leg, blank, blank, leg, blank],
  ];

  if (mode === "thinking") {
    sprite[0][6] = cell(c.mintBg);
    sprite[0][7] = cell(c.mintBg);
    sprite[1][6] = cell(c.mintBg);
    sprite[1][7] = blink ? cell(c.mintBg) : cell(c.blueBg);
  }

  if (mode === "done") {
    sprite[0][0] = cell(c.pinkBg);
    sprite[0][7] = cell(c.pinkBg);
  }

  if (mode === "error") {
    sprite[0][0] = cell(c.pinkBg);
    sprite[0][7] = cell(c.pinkBg);
    sprite[1][3] = cell(c.darkBg);
    sprite[1][5] = cell(c.darkBg);
  }

  return sprite.map((row) => row.join(""));
}

function statusText() {
  if (state.mode === "thinking") {
    return `${c.yellow}thinking${c.reset}`;
  }
  if (state.mode === "done") {
    return `${c.green}done${c.reset}`;
  }
  if (state.mode === "error") {
    return `${c.red}error${c.reset}`;
  }
  return `${c.gray}idle${c.reset}`;
}

function buildPromptLines() {
  const sprite = crabSprite(state.mode, state.blink);
  const args = forwardedArgs.length ? ` ${c.dim}[${forwardedArgs.join(" ")}]${c.reset}` : "";
  const line1 = `${sprite[0]}  ${c.bold}Crabdex${c.reset} ${statusText()}${args}`;
  const line2 = `${sprite[1]}  ${c.green}>${c.reset} ${state.running ? `${c.dim}waiting for Codex...${c.reset}` : state.input}`;
  const line3 = `${sprite[2]}`;
  const line4 = `${sprite[3]}  ${c.dim}/clear /exit${c.reset}`;
  return [line1, line2, line3, line4];
}

function clearPromptBlock() {
  if (!state.promptLines) {
    return;
  }
  readline.moveCursor(process.stdout, 0, -state.promptLines + 1);
  readline.cursorTo(process.stdout, 0);
  readline.clearScreenDown(process.stdout);
  state.promptLines = 0;
}

function refreshPrompt() {
  clearPromptBlock();
  const lines = buildPromptLines();
  for (let i = 0; i < lines.length; i += 1) {
    readline.clearLine(process.stdout, 0);
    readline.cursorTo(process.stdout, 0);
    process.stdout.write(lines[i]);
    if (i < lines.length - 1) {
      process.stdout.write("\n");
    }
  }
  state.promptLines = lines.length;
}

function printLine(text = "") {
  readline.clearLine(process.stdout, 0);
  readline.cursorTo(process.stdout, 0);
  process.stdout.write(text + "\n");
}

function printBlock(title, body, color) {
  clearPromptBlock();
  printLine(`${color}${title}${c.reset}`);
  const lines = String(body).replace(/\r/g, "").split("\n");
  for (const line of lines) {
    printLine(line);
  }
  printLine("");
  refreshPrompt();
}

function printBanner() {
  printLine(`${c.bold}Crabdex${c.reset} ${c.dim}PNG-style terminal sprite over codex exec${c.reset}`);
  printLine(`${c.gray}Using local icon-pack silhouette. Replies print below like a normal CLI.${c.reset}`);
  printLine("");
}

function parseJsonLines(buffer, onLine) {
  let rest = buffer;
  let idx = rest.indexOf("\n");
  while (idx >= 0) {
    const line = rest.slice(0, idx).trim();
    if (line) {
      onLine(line);
    }
    rest = rest.slice(idx + 1);
    idx = rest.indexOf("\n");
  }
  return rest;
}

function handleCommand(text) {
  if (text === "/exit") {
    shutdown(0);
    return true;
  }

  if (text === "/clear") {
    clearPromptBlock();
    process.stdout.write("\u001b[2J\u001b[H");
    printBanner();
    state.input = "";
    refreshPrompt();
    return true;
  }

  return false;
}

function runPrompt(prompt) {
  const trimmed = prompt.trim();
  if (!trimmed || state.running) {
    refreshPrompt();
    return;
  }

  if (handleCommand(trimmed)) {
    return;
  }

  printBlock("you", trimmed, c.cyan);

  state.running = true;
  state.mode = "thinking";
  state.input = "";
  refreshPrompt();

  const args = [
    CODEX_BIN,
    "exec",
    "--skip-git-repo-check",
    "--ephemeral",
    "--json",
    "--color",
    "never",
    "-C",
    process.cwd(),
    ...forwardedArgs,
    trimmed,
  ];

  const child = spawn("node", args, {
    cwd: process.cwd(),
    stdio: ["ignore", "pipe", "ignore"],
    env: process.env,
    windowsHide: false,
  });

  state.child = child;
  let stdoutRemainder = "";
  const answerParts = [];
  let sawCompletion = false;

  child.stdout.on("data", (chunk) => {
    stdoutRemainder = parseJsonLines(stdoutRemainder + chunk.toString("utf8"), (line) => {
      let event;
      try {
        event = JSON.parse(line);
      } catch {
        return;
      }

      if (event.type === "item.completed" && event.item && event.item.type === "agent_message") {
        answerParts.push(event.item.text || "");
      }

      if (event.type === "turn.completed") {
        sawCompletion = true;
      }
    });
  });

  child.on("error", (error) => {
    state.running = false;
    state.child = null;
    state.mode = "error";
    printBlock("error", `Failed to start Codex: ${error.message}`, c.red);
  });

  child.on("close", (code) => {
    state.running = false;
    state.child = null;

    const answer = answerParts.join("\n\n").trim();
    if (answer) {
      state.mode = "done";
      printBlock("codex", answer, c.green);
    } else if (code !== 0 || !sawCompletion) {
      state.mode = "error";
      printBlock(
        "error",
        "Codex returned no assistant message. Check native codex login or flags if this keeps happening.",
        c.red
      );
    } else {
      state.mode = "done";
      refreshPrompt();
    }
  });
}

function shutdown(code) {
  clearPromptBlock();
  if (state.child) {
    state.child.kill();
  }
  process.stdout.write(c.reset + "\n");
  process.exit(code);
}

function handleKey(buffer) {
  if (buffer.length === 1 && buffer[0] === 3) {
    shutdown(0);
    return;
  }

  if (state.running) {
    return;
  }

  if (buffer.length === 1 && buffer[0] === 13) {
    printLine("");
    runPrompt(state.input);
    return;
  }

  if (buffer.length === 1 && (buffer[0] === 8 || buffer[0] === 127)) {
    state.input = state.input.slice(0, -1);
    refreshPrompt();
    return;
  }

  const key = buffer.toString("utf8");
  if (key === "\u001b") {
    return;
  }

  if (/^[\x20-\x7E\u0400-\u04FF]+$/u.test(key)) {
    state.input += key;
    refreshPrompt();
  }
}

if (!process.stdin.isTTY || !process.stdout.isTTY) {
  console.error("Crabdex requires an interactive terminal.");
  process.exit(1);
}

process.on("SIGINT", () => shutdown(0));
process.stdout.on("resize", () => refreshPrompt());

process.stdin.setRawMode(true);
process.stdin.resume();
process.stdin.on("data", handleKey);

printBanner();
refreshPrompt();

setInterval(() => {
  state.tick += 1;
  state.blink = state.tick % 8 === 0;
  if (!state.running && state.mode === "done" && state.tick % 18 === 0) {
    state.mode = "idle";
  }
  refreshPrompt();
}, 220);