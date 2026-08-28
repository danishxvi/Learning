# Learning

A single, self-contained study repository for **Java**, **JavaScript**, **Node.js** and **React**.

Maintained by **Danish Husain**.

---

## What this repository is for

Most learning material is scattered. You read a blog for the syntax, a video for the
intuition, a Stack Overflow answer for the edge case, and the official docs for the
things the blog got wrong. This repository is an attempt to remove that scattering.

The goal is simple and deliberately ambitious:

> **After reading a file in this repository, you should not need to open another
> resource to understand that concept.**

To make that possible, every topic is delivered as **two files that share the same
number and name**:

| File | Purpose |
| --- | --- |
| `NN-topic-name.md` | The lesson. Theory, mental models, syntax tables, memory diagrams, common mistakes, interview-grade edge cases and a summary. Renders directly on GitHub, so you can learn from the browser on any device. |
| `NN-topic-name.<ext>` | The proof. A single, runnable, heavily commented program that demonstrates every claim the lesson makes. Run it, break it, change it. |

The files are numbered **sequentially across an entire stack**, so the learning order is
never ambiguous. Start at `01`, finish at the last number, and you will have walked the
whole language in the order concepts actually build on each other — never using an idea
before it has been explained.

---

## Repository layout

```
Learning/
├── Java/           Core Java, from `public static void main` to virtual threads
├── JavaScript/     The language itself, browser APIs and the modern ECMAScript feature set
├── Node.js/        Server-side JavaScript, Express, databases, auth, testing, deployment
└── React/          Components, hooks, routing, state management, performance, testing
```

Each stack folder has:

- its **own `README.md`** containing the full ordered syllabus with links, plus the exact
  commands needed to run that stack's examples,
- **numbered section folders** (`01-...`, `02-...`) that group related topics,
- the **`.md` + code file pairs** described above.

---

## How to use it

1. Open the stack you want to learn (say `Java/`) and read its `README.md`.
2. Open topic `01`, read the `.md`, then run the code file next to it.
3. Change the code. Break it on purpose. Read the error. That is the actual lesson.
4. Move to `02`. Do not skip — later files assume the earlier ones.

Every code file is standalone. You never need to have completed an earlier file for a
later one to compile or run.

---

## Setting up your machine

You need very little. Below is the bare minimum per stack, with verification commands.

### Common to everything

| Software | Why | Check it works |
| --- | --- | --- |
| A text editor — [VS Code](https://code.visualstudio.com/) is free and fine | Reading and editing files | — |
| [Git](https://git-scm.com/downloads) | Cloning this repository | `git --version` |

Clone once:

```bash
git clone https://github.com/danishxvi/Learning.git
```

```bash
cd Learning
```

---

### Java

**Install:** the JDK (Java Development Kit) — **version 21 or newer**. Grab it from
[Adoptium Temurin](https://adoptium.net/) (free, no account needed) or use your OS
package manager. The JDK includes everything; you do **not** need Maven, Gradle, an IDE,
or a project file for the lessons.

Verify both tools exist:

```bash
java -version
```

```bash
javac -version
```

**Run any lesson.** Since Java 11 a single-file program runs directly, with no separate
compile step:

```bash
java Java/01-getting-started/01-introduction-and-how-java-runs.java
```

If you prefer the classic two-step (or the file is part of a multi-class example):

```bash
javac -d out Java/01-getting-started/02-your-first-program.java
```

```bash
java -cp out YourFirstProgram
```

> **VS Code users:** installing the *Extension Pack for Java* adds a green ▶ Run button
> above `main`, which is the fastest way to work through these files.

---

### JavaScript

Two ways to run the lessons, and you will use both:

**1. In Node.js (for pure language topics — variables, closures, promises).**
Install [Node.js LTS](https://nodejs.org/) and run:

```bash
node JavaScript/01-language-foundations/01-how-javascript-runs.js
```

**2. In a browser (for DOM, events, storage, fetch).** Those lessons ship as `.html`
files. Just double-click the file, or right-click → *Open with* → your browser, then open
**DevTools** with `F12` and read the Console. No server or build tool is required.

> Anywhere a lesson prints with `console.log`, the output appears in your terminal (Node)
> or the DevTools Console (browser).

---

### Node.js

**Install:** [Node.js LTS](https://nodejs.org/) — version 20 or newer. It bundles `npm`,
so that is a single install.

```bash
node -v
```

```bash
npm -v
```

Sections that need third-party packages (Express, Mongoose, JWT, Jest…) contain their own
`package.json`. Install inside that folder only:

```bash
cd Node.js/03-express-fundamentals
```

```bash
npm install
```

```bash
node 12-express-first-server.js
```

Single-file lessons that use only Node's built-in modules need **no `npm install` at
all** — just `node <file>`.

A few later sections need a database. Each of those states its requirement at the top of
its `.md`, and every one of them offers a free cloud option (such as MongoDB Atlas) so
you never have to install a database server locally if you would rather not.

---

### React

**Install:** [Node.js LTS](https://nodejs.org/) — that is the only requirement.

Early React lessons are single files you can read on their own. The runnable playground
lives in one Vite project shared by the whole stack:

```bash
cd React/playground
```

```bash
npm install
```

```bash
npm run dev
```

Open the printed URL (usually `http://localhost:5173`). Each lesson tells you which
component to mount in `src/App.jsx` to see it live.

---

## A note on the code style

The code in this repository is written to be *read*, not to be clever. Expect:

- long, descriptive names over short ones,
- comments that explain **why**, not what,
- deliberate mistakes shown next to the fix, clearly marked,
- and output printed at every interesting step so you can see the concept happen instead
  of taking its word for it.

---

## Progress

This repository is being built stack by stack. Each stack's `README.md` marks which
topics are complete, so you always know where the finished edge is.

| Stack | Status |
| --- | --- |
| Java | In progress |
| JavaScript | Planned |
| Node.js | Planned |
| React | Planned |
