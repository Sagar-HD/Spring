# ⚡ JavaScript — From First Principles to Official Runtime Mechanics



> ## 🧠 Don't memorize JavaScript. Follow the problems. Watch JavaScript solve them.
>
>
>
>
> By building a complete mental model of what happens underneath, the language stops looking like random features and starts looking like one connected machine.
>
>



# 🗺️ The Big Picture Journey



```text
🌍 "I wrote some JavaScript..."[cite: 1]
          │
          ▼
🧠 Who executes it? (Engine vs. Runtime)[cite: 1, 2]
          │
          ▼
🌳 Parsing & Compilation (AST, JIT, Interpreter)[cite: 1, 2]
          │
          ▼
📦 Execution Context & Variable Environment[cite: 1, 2]
          │
          ▼
📚 Call Stack (LIFO Execution)[cite: 1, 2]
          │
          ▼
👻 Declaration Processing & Hoisting[cite: 1, 2]
          │
          ▼
🏠 Scope, Lexical Environment & Scope Chain[cite: 1, 2]
          │
          ▼
☢️ Variable Declarations (var, let, const, TDZ)[cite: 1, 2]
          │
          ▼
🔒 Closures & Memory Lifecycle[cite: 1, 2]
          │
          ▼
🎁 Functions as Values (First-Class Citizens)[cite: 1, 2]
          │
          ▼
📞 Callbacks & Higher-Order Functions[cite: 1, 2]
          │
          ▼
🌐 Host APIs (Browser / Node.js Runtime)[cite: 1, 2]
          │
          ▼
🔄 Event Loop (Tasks vs. Microtasks)[cite: 1, 2]
          │
          ▼
🤝 Promises & Async Flow Control[cite: 1, 2]
          │
          ▼
⏳ async / await Execution Mechanics[cite: 1, 2]
          │
          ▼
🧭 Execution Binding (`this` keyword)[cite: 1]
          │
          ▼
🚀 React & Framework Paradigms[cite: 1, 2]
```[cite: 1, 2]

---

# 1️⃣ Before Execution: What Actually Runs JavaScript?[cite: 1, 2]

When you write a line of code like `var x = 10;`, neither the `.js` file, the browser, nor the JavaScript engine acts entirely alone.[cite: 1]

JavaScript is a language specified by **ECMAScript**[cite: 1, 2]. A **JavaScript Engine** (like V8, SpiderMonkey, or JavaScriptCore) is the core software component that executes code[cite: 1, 2]. The **Runtime Environment** (like Chrome or Node.js) wraps around the engine to provide host capabilities[cite: 1, 2].

```text
┌────────────────────────────────────────────────────────┐
│               JAVASCRIPT RUNTIME ENVIRONMENT           │[cite: 1, 2]
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │               JS ENGINE 🧠                       │  │[cite: 1, 2]
│  │                                                  │  │
│  │  Parse ──► Compile ──► Execute                  │  │[cite: 1, 2]
│  │  Memory / Heap ──► GC ──► Call Stack             │  │[cite: 1, 2]
│  └────────────────────────┬─────────────────────────┘  │
│                           │                            │
│                  Host Infrastructure                   │[cite: 1, 2]
│                           │                            │
│  ┌────────────────────────▼─────────────────────────┐  │
│  │               HOST APIs 🌐                       │  │[cite: 1, 2]
│  │  Browser: DOM, Timers, fetch, Storage, Events... │  │[cite: 1, 2]
│  │  Node.js: fs, http, path, process...              │  │[cite: 1, 2]
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```[cite: 1, 2]

### 🌐 Browser vs. Engine Distinction[cite: 1, 2]
A browser does **not simply create the Global Execution Context and ask V8 to push it**.[cite: 2] 
* **The Engine** manages JavaScript execution, parses source code, allocates memory, handles garbage collection, and maintains execution contexts[cite: 1, 2].
* **The Host Environment** provides surrounding Web APIs (DOM, `fetch`, timers) and host object primitives (`window`, `document`)[cite: 1, 2].
* **Node.js** is another runtime using V8, but replaces Web APIs with system APIs (`fs`, `net`, `http`), lacking `window` or DOM capabilities[cite: 1, 2].

---

# 2️⃣ JS Engine Pipeline: Parse → Compile/Optimize → Execute[cite: 1, 2]

Computers cannot execute high-level English-like syntax directly.[cite: 1, 2] The source code undergoes strict transformation before machine evaluation[cite: 1, 2].

```text
Source Code Text
       │
       ▼
🔤 Lexical Analysis / Tokenization
       │
       ▼
🌳 Parsing ──► Abstract Syntax Tree (AST)
       │
       ▼
⚙️ Interpreter (e.g., Ignition in V8) ──► Bytecode
       │
       ├───► 🔥 Hot Code Detection (Profiling)
       │          │
       │          ▼
       └──── JIT Compiler (e.g., TurboFan) ──► Optimized Machine Code
                  │
                  ▼
          (Deoptimization fallback if assumptions fail)
```[cite: 1, 2]

### Abstract Syntax Tree (AST) Visual[cite: 1, 2]
For the expression `const total = 10 + 20;`[cite: 1, 2]:

```text
          VariableDeclaration (const)
                     │
                     ▼
             Assignment (=)
            /              \
     Identifier (total)   BinaryExpression (+)
                         /                  \
                    Literal (10)        Literal (20)
```[cite: 1, 2]

### Is JavaScript Interpreted or Compiled?[cite: 1, 2]
* **Traditional Model:** JavaScript was pure interpretation (line-by-line reading)[cite: 1, 2].
* **Modern Reality:** Engines use **Just-In-Time (JIT) Compilation**[cite: 1, 2]. Code is initially interpreted into bytecode for fast startup[cite: 1, 2]. Frequently called functions ("hot code") are passed to an optimizing compiler to generate high-performance machine code[cite: 1, 2]. If dynamic type assumptions change at runtime, the engine performs **deoptimization** and reverts to bytecode[cite: 1, 2].

---

# 3️⃣ Execution Context — The Unit of Execution 📦[cite: 1, 2]

An **Execution Context** is the fundamental runtime unit that tracks the environment state required to evaluate JavaScript code[cite: 1, 2].

```text
┌─────────────────────────────────────────────────────────┐
│                   EXECUTION CONTEXT                     │[cite: 1, 2]
├─────────────────────────────────────────────────────────┤
│ 🧠 Environment Record / Bindings                        │[cite: 1, 2]
│    • Variable declarations & function bindings          │[cite: 1, 2]
│    • Parameter values                                   │[cite: 2]
├─────────────────────────────────────────────────────────┤
│ 🔗 Lexical Environment Reference (Scope Link)           │[cite: 1, 2]
│    • Pointer to outer environment chain                 │[cite: 1, 2]
├─────────────────────────────────────────────────────────┤
│ 🧭 `this` Binding                                       │[cite: 2]
│    • Context reference determined at invocation         │[cite: 1, 2]
└─────────────────────────────────────────────────────────┘
```[cite: 1, 2]

### Conceptual Phase Mapping[cite: 1, 2]
While learning models split execution contexts into "Memory Phase" and "Code Execution Phase", official specifications define these as[cite: 1, 2]:
1. **Creation / Binding Instantiation Phase:** Declaration processing, identifier resolution, binding instantiation, and scope chain creation before line-by-line evaluation[cite: 1, 2].
2. **Execution / Evaluation Phase:** Line-by-line evaluation of statements, assignment operations, and function calls[cite: 1, 2].

---

# 4️⃣ Global Execution Context & Function Execution Worlds 🪆[cite: 1, 2]

When a script starts, the **Global Execution Context (GEC)** is created first[cite: 1, 2].

```js
var x = 10;

function a() {
    var x = 20;
    console.log(x);
}

function b() {
    var x = 100;
    console.log(x);
}

a();
b();
console.log(x);
```[cite: 1, 2]

### Lifecycle Execution Trace[cite: 1, 2]:

```text
1. Preparation / Creation Phase (GEC):
   x ──► undefined
   a ──► [Function Object: a]
   b ──► [Function Object: b]

2. Evaluation Phase (GEC):
   x = 10
   Invoke a() ──► Creates Function Execution Context (FEC) for a()
                  │
                  ├── Memory Creation: x ──► undefined
                  └── Execution:       x = 20; console.log(x) // Prints 20
                  FEC for a() finishes ──► Popped from stack

   Invoke b() ──► Creates Function Execution Context (FEC) for b()
                  │
                  ├── Memory Creation: x ──► undefined
                  └── Execution:       x = 100; console.log(x) // Prints 100
                  FEC for b() finishes ──► Popped from stack

   console.log(x) ──► Reads global x // Prints 10
```[cite: 1, 2]

---

# 5️⃣ The Call Stack 🥞[cite: 1, 2]

The **Call Stack** (or Execution Context Stack) manages active execution contexts using a strict **LIFO (Last In, First Out)** structure[cite: 1, 2].

```js
function one() { two(); }
function two() { three(); }
function three() { console.log("Stack Top"); }
one();
```[cite: 2]

```text
    STACK STATE TRANSITIONS OVER TIME:

    │             │    │             │    │  three()    │    │             │
    │             │    │  two()      │    │  two()      │    │  two()      │
    │  one()      │    │  one()      │    │  one()      │    │  one()      │
    │  Global EC  │    │  Global EC  │    │  Global EC  │    │  Global EC  │
    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       Step 1             Step 2             Step 3             Step 4
     Call one()         Call two()        Call three()     three() returns
```[cite: 1, 2]

* **Function Call:** Push new execution context to the top of stack[cite: 1, 2].
* **Function Return:** Pop top execution context off the stack[cite: 1, 2].
* **Program Completion:** Top-level global execution completes; stack becomes empty[cite: 1, 2].

---

# 6️⃣ Hoisting: Declaration vs. Initialization 👻[cite: 1, 2]

Hoisting describes the observable behavior caused by declaration processing during environment setup prior to step-by-step code evaluation[cite: 1, 2]. It does **not** move source code physically[cite: 1, 2].

```js
console.log(x); // undefined
var x = 10;

greet(); // "Hello"
function greet() { console.log("Hello"); }
```[cite: 1, 2]

### Declaration Behavior Before Execution Line[cite: 1, 2]:

| Declaration Type | Binding Created? | Value Before Declaration Line | Attempting Early Call / Access |
|---|---|---|---|
| `var` | Yes | `undefined` | Reads `undefined`[cite: 1, 2] |
| Function Declaration | Yes | Function Object | Executes successfully[cite: 1, 2] |
| `let` | Yes | Uninitialized (in TDZ) | Throws `ReferenceError`[cite: 1, 2] |
| `const` | Yes | Uninitialized (in TDZ) | Throws `ReferenceError`[cite: 1, 2] |
| `class` | Yes | Uninitialized (in TDZ) | Throws `ReferenceError`[cite: 2] |

### Function Declaration vs. Function Expression vs. Arrow Function[cite: 1, 2]

```js
// 1. Function Declaration
fnOne(); // Works!
function fnOne() {}

// 2. Function Expression (var)
fnTwo(); // TypeError: fnTwo is not a function
var fnTwo = function() {};

// 3. Arrow Function Expression (let)
fnThree(); // ReferenceError: Cannot access 'fnThree' before initialization
let fnThree = () => {};
```[cite: 1, 2]

### Named Function Expressions & Self-Reference[cite: 2]
```js
const factorial = function fact(n) {
    if (n <= 1) return 1;
    return n * fact(n - 1); // Internal self-reference via 'fact'
};
factorial(5); // 120
```[cite: 2]
* `factorial` is bound in the outer scope[cite: 2].
* `fact` is locally bound *only* inside the function's own internal scope, useful for recursion[cite: 2].

---

# 7️⃣ Parameter vs. Argument & Identifier Resolution 🚨[cite: 1, 2]

* **Parameters:** Names declared in the function definition (`function add(a, b)`)[cite: 2].
* **Arguments:** Actual values passed into the function during invocation (`add(10, 20)`)[cite: 2].

### `undefined` vs. "Not Defined" (`ReferenceError`)[cite: 1, 2]

```js
var x;
console.log(x); // undefined

console.log(y); // Uncaught ReferenceError: y is not defined
```[cite: 1, 2]

```text
undefined:
x ──► Binding exists in Environment Record ──► Current Value = `undefined`

ReferenceError:
y ──► Search local scope ──► Not found ──► Search outer scope ──► Not found ──► Reaches Top ──► 💥 ReferenceError
```[cite: 1, 2]

---

# 8️⃣ Scope, Lexical Environments & Scope Chain 🏠[cite: 1, 2]

**Scope** determines where identifiers are visible and accessible[cite: 1, 2].
**Lexical Scoping** means scope structure is determined by **where functions and blocks are written in the source code**, not where they are called[cite: 1, 2].

```js
const a = "A";

function x() {
    const b = "B";

    function y() {
        const c = "C";
        console.log(a, b, c);
    }
    y();
}
x();
```[cite: 1, 2]

```text
LEXICAL ENVIRONMENT LOOKUP CHAIN (SCOPE CHAIN):

y() Lexical Environment
┌──────────────────────────────────────┐
│ Environment Record: { c: "C" }       │
│ Outer Env Reference: ────────────────┼───┐
└──────────────────────────────────────┘   │
                                           ▼
x() Lexical Environment
┌──────────────────────────────────────┐
│ Environment Record: { b: "B" }       │
│ Outer Env Reference: ────────────────┼───┐
└──────────────────────────────────────┘   │
                                           ▼
Global Lexical Environment
┌──────────────────────────────────────┐
│ Environment Record: { a: "A" }       │
│ Outer Env Reference: null            │
└──────────────────────────────────────┘
```[cite: 1, 2]

### Global Scope, Global Object & `globalThis`[cite: 1, 2]
In classic browser scripts, `var` at top-level creates a property on the global object (`window`)[cite: 2]. However, top-level `let` and `const` create top-level lexical bindings that do **not** attach to `window`[cite: 2].
To unify access across environments (Browser, Node.js, Web Workers), ECMAScript introduced **`globalThis`**[cite: 2].

---

# 9️⃣ Variable Declarations: `var` vs `let` vs `const` & TDZ ☢️[cite: 1, 2]

```text
DECLARATION COMPARISON:

var:   [ Function Scoped ] [ Reassignable ] [ Re-declarable ] [ No TDZ ]
let:   [ Block Scoped    ] [ Reassignable ] [ Unique Scope  ] [ Has TDZ ]
const: [ Block Scoped    ] [ Immutable    ] [ Unique Scope  ] [ Has TDZ ]
                           Binding Only
```[cite: 1, 2]

### Temporal Dead Zone (TDZ) Visualized[cite: 1, 2]

The **Temporal Dead Zone** is the period between entering a scope and evaluating the binding's initialization line[cite: 1, 2].

```text
Scope Entered
│
├── 🚫 TDZ Starts
│   console.log(x); // ReferenceError: Cannot access 'x' before initialization
│   var y = x + 1;  // ReferenceError
├── 🚫 TDZ Ends
│
let x = 10; // Initialization Evaluated
│
└── ✅ x is accessible (x === 10)
```[cite: 1, 2]

### `const` Reassignment vs. Mutation[cite: 1, 2]
`const` protects the variable binding pointer, not the underlying object value[cite: 1, 2].

```js
const user = { name: "Sagar" };
user.name = "AIT";  // ✅ Allowed: Mutates property inside object
user = { name: "New" }; // ❌ TypeError: Assignment to constant variable.
```[cite: 1, 2]

---

# 🔟 Block Scope, Shadowing & Illegal Shadowing 🧱[cite: 1, 2]

A block `{ ... }` scopes `let` and `const` declarations[cite: 1, 2]. `var` declarations ignore block boundaries and hoist to the nearest enclosing function or global scope[cite: 1, 2].

```js
let x = 10;
var y = 100;

{
    let x = 20; // Legal Shadowing: Inner x shadows outer x
    var y = 200; // Overwrites outer y!
    console.log(x); // 20
}

console.log(x); // 10
console.log(y); // 200
```[cite: 1, 2]

### Illegal Shadowing Rules 🚫[cite: 1, 2]

```js
// ❌ Illegal Shadowing (SyntaxError)
let a = 10;
{
    var a = 20; // SyntaxError: Identifier 'a' has already been declared
}
// Reason: 'var' attempts to hoist to outer scope, colliding with lexical 'let a'.

// ✅ Legal Shadowing inside Function Boundary
let b = 10;
function test() {
    var b = 20; // OK: 'var' is constrained by new function scope boundary
}
```[cite: 1, 2]

### Common JavaScript Errors[cite: 2]
* **`SyntaxError`:** Code violates language grammar or duplicate lexical bindings exist in the same scope[cite: 2].
* **`ReferenceError`:** Identifier resolution fails to find an accessible binding or accesses a binding in TDZ[cite: 1, 2].
* **`TypeError`:** Value is of wrong type for requested operation (e.g., calling non-function, reassigning `const`)[cite: 1, 2].

---

# 1️⃣1️⃣ Closures — Retaining Scope Across Lifecycles 🔥[cite: 1, 2]

> **Official Definition:** A closure is the combination of a function bundled together (enclosed) with references to its surrounding lexical environment[cite: 1, 2].

```js
function outer() {
    const x = 10;
    function inner() {
        console.log(x);
    }
    return inner;
}

const fn = outer();
fn(); // Prints 10!
```[cite: 1, 2]

Even after `outer()` finishes executing and its execution context is popped off the Call Stack, `inner()` retains access to `x`[cite: 1, 2].

```text
        Call Stack                     Heap Memory / Closures
┌─────────────────────────┐         ┌─────────────────────────┐
│ [ Popped outer() EC ]   │         │ Closure Scope (outer)   │
├─────────────────────────┤         │ ┌─────────────────────┐ │
│ fn() Execution Context  │─────────┼─► x = 10              │ │
├─────────────────────────┤         │ └─────────────────────┘ │
│ Global Execution Context│         │ Accessible via fn()     │
└─────────────────────────┘         └─────────────────────────┘
```[cite: 1, 2]

---

# 1️⃣2️⃣ Practical Closures & The Loop Problem ⏰[cite: 1, 2]

### The Classic `setTimeout` Bug[cite: 1, 2]
```js
for (var i = 1; i <= 3; i++) {
    setTimeout(() => {
        console.log(i);
    }, i * 1000);
}
// Output after 1s, 2s, 3s: 4, 4, 4
```[cite: 1, 2]
* **Why?** `var` is function-scoped[cite: 1, 2]. All 3 timer callbacks close over the exact same single `i` variable binding, which reaches `4` when the loop completes[cite: 1, 2].

### Solution 1: Block Scoped `let`[cite: 1, 2]
```js
for (let i = 1; i <= 3; i++) {
    setTimeout(() => console.log(i), i * 1000);
}
// Output: 1, 2, 3
```[cite: 1, 2]
* `let` creates a distinct, new lexical binding for `i` on every loop iteration[cite: 1, 2].

### Solution 2: Explicit Closure via Function Wrapper (IIFE)[cite: 1, 2]
```js
for (var i = 1; i <= 3; i++) {
    (function(value) {
        setTimeout(() => console.log(value), value * 1000);
    })(i);
}
// Output: 1, 2, 3
```[cite: 1, 2]

### Data Privacy & Encapsulation[cite: 1, 2]
```js
function createCounter() {
    let count = 0; // Private lexical state
    return {
        increment() { count++; return count; },
        decrement() { count--; return count; }
    };
}
const c1 = createCounter();
const c2 = createCounter();
c1.increment(); // 1
console.log(c1.count); // undefined (Protected from direct outside access!)
```[cite: 1, 2]

---

# 1️⃣3️⃣ Constructor Functions vs. Closures 🏗️[cite: 1, 2]

Before ES6 `class` syntax, constructors initialized object state[cite: 1, 2]:

```js
function Counter() {
    this.count = 0;
    this.increment = function() {
        this.count++;
    };
}
const counterInstance = new Counter();
```[cite: 1, 2]

### The `new` Operator Step-by-Step[cite: 1, 2]:
1. Creates a brand-new plain object `{}`[cite: 1, 2].
2. Links the object's internal `[[Prototype]]` to `Counter.prototype`[cite: 1, 2].
3. Calls `Counter` constructor with `this` bound to the new object[cite: 1, 2].
4. Returns the new object automatically (unless constructor returns another object explicitly)[cite: 1, 2].

```text
Closure Counter:      State stored in closed-over Lexical Environment (Private)[cite: 2]
Constructor Counter:  State stored as properties directly on object instance (Public)[cite: 2]
```[cite: 2]

---

# 1️⃣4️⃣ First-Class Functions & Functional Paradigms 🎁[cite: 1, 2]

Functions in JavaScript are **First-Class Citizens** (Objects of type `Function`)[cite: 1, 2].

```text
FIRST-CLASS POWERS:
1. Assign to variables:      const fn = function() {};[cite: 1, 2]
2. Pass as arguments:        execute(function() {});[cite: 1, 2]
3. Return from functions:    function outer() { return function() {}; }[cite: 1, 2]
4. Store in properties:      const obj = { run: function() {} };[cite: 2]
```[cite: 1, 2]

### Higher-Order Functions (HOF)[cite: 1, 2]
A Higher-Order Function accepts another function as an argument, returns a function, or both[cite: 1, 2].

```js
// Generic algorithm abstraction
function calculate(array, logic) {
    const output = [];
    for (let item of array) {
        output.push(logic(item));
    }
    return output;
}
const radii = [1, 2, 3];
const area = r => Math.PI * r * r;
console.log(calculate(radii, area));
```[cite: 1, 2]

### Extending `Array.prototype` (Polyfill Model)[cite: 1, 2]
```js
Array.prototype.customMap = function(logic) {
    const result = [];
    for (let i = 0; i < this.length; i++) {
        result.push(logic(this[i], i, this));
    }
    return result;
};
```[cite: 1, 2]
* ⚠️ **Production Note:** Avoid mutating built-in prototypes in production apps to prevent library conflicts and monkey-patching bugs[cite: 2].

---

# 1️⃣5️⃣ Essential Array Transformations: Map, Filter, Reduce 🧪[cite: 1, 2]

```text
INPUT ARRAY: [ 1, 2, 3, 4 ]

.map(x => x * 2) ──────► Transformation ─────► [ 2, 4, 6, 8 ] (Same length)[cite: 1, 2]

.filter(x => x % 2 === 0) ──► Selection ───────► [ 2, 4 ] (Length <= Input)[cite: 1, 2]

.reduce((acc, curr) => acc + curr, 0) ───────► Single Result ──► 10 (Accumulated)[cite: 1, 2]
```[cite: 1, 2]

### Visual Accumulation Flow of `reduce()`[cite: 1, 2]:
```js
const total = [1, 2, 3].reduce((acc, curr) => acc + curr, 0);
```[cite: 1, 2]

```text
Initial Value: acc = 0
Step 1: acc (0) + curr (1) ──► New acc = 1
Step 2: acc (1) + curr (2) ──► New acc = 3
Step 3: acc (3) + curr (3) ──► Final Result = 6
```[cite: 1, 2]

---

# 1️⃣6️⃣ Async Architecture & The Event Loop 🌐[cite: 1, 2]

JavaScript execution is **synchronous and single-threaded** (one call stack)[cite: 1, 2]. Asynchronous behavior is made possible by the surrounding **Host Environment APIs** and the **Event Loop**[cite: 1, 2].

```text
                                HOST ENVIRONMENT (Browser / Node)
                                ┌────────────────────────────────┐
                                │ Web APIs                       │
                                │ • Timers (setTimeout)          │
                                │ • Fetch / HTTP Requests        │
                                │ • DOM Events                   │
                                └───────────────┬────────────────┘
                                                │ Task Completed
                                                ▼
┌────────────────────────┐      ┌────────────────────────────────┐
│   CALL STACK 📚        │      │   MICROTASK QUEUE ⚡           │
│   (Executing JS)       │      │   • Promise Reactions (.then)  │
│                        │      │   • MutationObserver           │
│   [ fn() ]             │      └───────────────┬────────────────┘
│   [ Global Context ]   │                      │ High Priority
└──────────▲─────────────┘                      ▼
           │                     ┌──────────────────────────────┐
           │                     │   TASK QUEUE / MACROTASK 📬  │
           └─────────────────────┤   • setTimeout / setInterval │
                Event Loop       │   • DOM Events / I/O         │
                Checks           └──────────────────────────────┘
```[cite: 1, 2]

### The Event Loop Rule[cite: 1, 2]
1. Execute synchronous code on the **Call Stack**[cite: 1, 2].
2. When Call Stack becomes empty, drain **ALL Microtasks** in the Microtask Queue completely[cite: 1, 2].
3. If Microtask Queue is clear, allow rendering (in browsers) and pick **ONE Task** from the Task Queue[cite: 1, 2].
4. Repeat endlessly[cite: 1, 2].

### 😈 Microtask Starvation
If a microtask continuously queues another microtask recursively, the event loop will stay trapped draining microtasks forever, starving the Task Queue and blocking UI rendering[cite: 1, 2].

---

# 1️⃣7️⃣ Promises — Taming Async & Inversion of Control 🤝[cite: 1, 2]

### The Problem: Callback Hell & Inversion of Control[cite: 1, 2]

```js
// 💀 Pyramid of Doom & Inversion of Control
createOrder(cart, function(orderId) {
    proceedToPayment(orderId, function(paymentInfo) {
        showSummary(paymentInfo, function() {
            updateWallet();
        });
    });
});
```[cite: 1, 2]
* **Inversion of Control:** Passing your callback to a third-party function forces you to blindly trust that it will call your function, call it only once, and pass correct parameters[cite: 1, 2].

### The Promise Solution[cite: 1, 2]
Instead of passing a callback into a function, an asynchronous function returns an immutable **Promise object** representing the future result[cite: 1, 2].

```text
                        ┌──────────────────────────────┐
                        │        PROMISE OBJECT        │[cite: 1, 2]
                        ├──────────────────────────────┤
                        │ State:  pending              │[cite: 1, 2]
                        │ Result: undefined            │[cite: 1, 2]
                        └──────────────┬───────────────┘
                                       │ Settles
                       ┌───────────────┴───────────────┐
                       ▼                               ▼
        ┌──────────────────────────────┐┌──────────────────────────────┐
        │ State:  fulfilled            ││ State:  rejected             │[cite: 1, 2]
        │ Result: value                ││ Result: error                │[cite: 1, 2]
        └──────────────────────────────┘└──────────────────────────────┘
```[cite: 1, 2]

```js
createOrder(cart)
    .then(orderId => proceedToPayment(orderId))
    .then(paymentInfo => showSummary(paymentInfo))
    .then(() => updateWallet())
    .catch(err => console.error(err));
```[cite: 1, 2]

---

# 1️⃣8️⃣ Promise Combinators Reference 🧰[cite: 1]

```js
// Given an array of promises: [p1, p2, p3]
```[cite: 1]

| Method | Settles When... | Fulfills With... | Rejects When... |
|---|---|---|---|
| **`Promise.all()`**[cite: 1] | All fulfill OR any rejects | Array of all resolved values[cite: 1] | Immediately upon **first rejection** (Fast-Fail)[cite: 1] |
| **`Promise.allSettled()`**[cite: 1] | All settle (fulfill or reject) | Array of outcome objects `{ status, value/reason }`[cite: 1, 2] | Never rejects overall[cite: 1] |
| **`Promise.race()`**[cite: 1] | First promise settles | Value of the **first settled** promise[cite: 1] | Reason of the **first settled** promise if it rejected first[cite: 1] |
| **`Promise.any()`**[cite: 1] | First fulfills OR all reject | Value of the **first fulfilled** promise[cite: 1] | Rejects with `AggregateError` if **all fail**[cite: 1] |

---

# 1️⃣9️⃣ `async` / `await` Under the Hood ⏳[cite: 1, 2]

`async/await` is syntactic sugar over Promises and Generators[cite: 1, 2].

```js
async function loadData() {
    console.log("1");
    const data = await fetch("/api");
    console.log("2");
}
```[cite: 1, 2]

```text
EXECUTION STEP-BY-STEP:

1. loadData() invoked synchronous execution starts ──► Prints "1"[cite: 1, 2]
2. Reaches await fetch("/api"):[cite: 1, 2]
   • fetch() starts networking work via Browser Host APIs[cite: 1, 2]
   • Returns a pending Promise[cite: 1, 2]
   • ⏸️ loadData() function execution SUSPENDS[cite: 1, 2]
   • loadData() pops off Call Stack; main thread continues other work[cite: 1, 2]
3. Network request completes ──► Promise fulfills[cite: 1, 2]
4. Resumption job scheduled in MICROTASK QUEUE[cite: 1, 2]
5. Call Stack clears ──► Event Loop moves job to Call Stack[cite: 1, 2]
6. ⏯️ loadData() RESUMES execution ──► Prints "2"[cite: 1, 2]
```[cite: 1, 2]

### Sequential vs. Concurrent Await 🧠[cite: 1]

```js
// ❌ Slow Sequential Waterfall: Total time = timeA + timeB
const a = await getA(); 
const b = await getB();

// ✅ Fast Concurrent Execution: Total time = Math.max(timeA, timeB)
const promiseA = getA();
const promiseB = getB();
const [a, b] = await Promise.all([promiseA, promiseB]);
```[cite: 1]

### `fetch()` Has 2 Async Layers 🌊[cite: 1]
```js
const response = await fetch("/api/user"); // Layer 1: HTTP Headers received
const data = await response.json();        // Layer 2: Async Stream parsing body
```[cite: 1]
* **`fetch()` HTTP Errors:** `fetch()` does **NOT** reject on HTTP `404` or `500` errors[cite: 1, 2]. It resolves successfully with `response.ok = false`[cite: 1, 2]. Rejection happens only on network failures or blocked requests[cite: 1, 2].

---

# 2️⃣0️⃣ The `this` Keyword Mechanics 🧭[cite: 1]

For regular functions, `this` is not determined by where the function is defined, but by **HOW it is invoked** at call-time[cite: 1].

```text
                                HOW WAS THE FUNCTION CALLED?[cite: 1]
                                              │
         ┌────────────────────────────────────┼────────────────────────────────────┐
         │                                    │                                    │
         ▼                                    ▼                                    ▼
[ Method Invocation ]               [ Plain Function Call ]              [ Explicit Binding ][cite: 1]
   obj.method()                        fn()                                 fn.call(ctx)
   this === obj                        Non-strict: window/global            fn.apply(ctx)
                                       Strict:     undefined                fn.bind(ctx)[cite: 1]
                                                                            this === ctx[cite: 1]
```[cite: 1]

### Code Patterns & Scenarios[cite: 1]:

```js
// 1. Method Invocation
const user = {
    name: "Sagar",
    greet() { console.log(this.name); }
};
user.greet(); // "Sagar" (this -> user)

// 2. Dangerous Extraction Trap
const extracted = user.greet;
extracted(); // undefined (this -> global/undefined due to plain call!)

// 3. Arrow Functions: Lexical `this`
const obj = {
    name: "Sagar",
    arrow: () => {
        console.log(this.name); // Uses 'this' from outer enclosing scope!
    }
};
obj.arrow(); // undefined (outer scope is global/module)

// 4. Explicit Binding Methods
function show(a, b) { console.log(this, a, b); }
show.call({ id: 1 }, 10, 20);  // Direct arg list
show.apply({ id: 1 }, [10, 20]); // Array arg list
const boundFn = show.bind({ id: 1 }); // Returns new bound function
boundFn(10, 20);
```[cite: 1]

---

# 2️⃣1️⃣ Master Architecture Diagram 🧠🌐[cite: 1]

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           JAVASCRIPT RUNTIME ENVIRONMENT                        │[cite: 1, 2]
│                                                                                 │
│   ┌─────────────────────────────────────────────────────────────────────────┐   │
│   │                      JAVASCRIPT ENGINE (e.g. V8)                        │   │[cite: 1, 2]
│   │                                                                         │   │
│   │   Source ──► Parse ──► AST ──► Bytecode ──► JIT Compiler ──► Machine Code   │   │[cite: 1, 2]
│   │                                                                         │   │
│   │   ┌───────────────────────────────┐   ┌─────────────────────────────┐   │   │
│   │   │        MEMORY HEAP            │   │         CALL STACK          │   │   │[cite: 1, 2]
│   │   │  • Object allocations         │   │                             │   │   │
│   │   │  • Closures & Lexical Env     │   │  [ Function Execution EC ]  │   │   │[cite: 1, 2]
│   │   │  • Garbage Collector (GC)     │   │  [ Global Execution EC   ]  │   │   │[cite: 1, 2]
│   │   └───────────────────────────────┘   └──────────────┬──────────────┘   │   │
│   └──────────────────────────────────────────────────────┼──────────────────┘   │
│                                                          │                      │
│                                                  Synchronous Work               │[cite: 1, 2]
│                                                          │                      │
│   ┌──────────────────────────────────────────────────────▼──────────────────┐   │
│   │                     HOST ENVIRONMENT APIS (Web APIs)                    │   │[cite: 1, 2]
│   │      DOM | Timers (setTimeout) | fetch / Network | Storage | Events     │   │[cite: 1, 2]
│   └──────────────────────────────────────┬──────────────────────────────────┘   │
│                                          │ Async Completion Callbacks           │
│                                          │                                      │
│                  ┌───────────────────────┴───────────────────────┐              │
│                  ▼                                               ▼              │
│   ┌──────────────────────────────┐               ┌──────────────────────────┐   │
│   │    MICROTASK QUEUE ⚡        │               │     TASK QUEUE 📬        │   │[cite: 1, 2]
│   │  • Promise reactions (.then) │               │  • Timer callbacks      │   │[cite: 1, 2]
│   │  • MutationObserver          │               │  • Event Listeners       │   │[cite: 1, 2]
│   └──────────────┬───────────────┘               └───────────┬──────────────┘   │
│                  │ Priority                                  │                  │
│                  └───────────────────────┬───────────────────┘                  │
│                                          │                                      │
│                                          ▼                                      │
│                                   EVENT LOOP 🚦                                 │[cite: 1, 2]
│                        Checks: Stack Empty? Drain Microtasks                    │[cite: 1, 2]
│                                          │                                      │
│                                          └──────────────────────────────────────┘
└─────────────────────────────────────────────────────────────────────────────────┘
```[cite: 1, 2]

---

# 2️⃣2️⃣ Essential Technical Misconceptions 🚨[cite: 1, 2]

1. **❌ "Hoisting physically moves code to the top of the file."**[cite: 1, 2]
   * **Reality:** Hoisting is declaration space allocation during environment context creation before line evaluation[cite: 1, 2].
2. **❌ "`let` and `const` are not hoisted."**[cite: 1, 2]
   * **Reality:** They are hoisted, but their access is blocked by the Temporal Dead Zone (TDZ) prior to initialization[cite: 1, 2].
3. **❌ "JavaScript is multithreaded because `fetch` runs asynchronously."**[cite: 1, 2]
   * **Reality:** JavaScript execution on the stack is strictly single-threaded[cite: 1, 2]. Non-blocking network operations run on host C++ worker threads inside the browser/runtime environment[cite: 1, 2].
4. **❌ "`async/await` blocks the JavaScript main thread thread while waiting."**[cite: 1, 2]
   * **Reality:** `await` suspends *only* the specific `async` function execution[cite: 1, 2]. The thread unwinds and remains free to run other tasks[cite: 1, 2].
5. **❌ "`setTimeout(fn, 1000)` guarantees execution in exactly 1000ms."**[cite: 1, 2]
   * **Reality:** `1000ms` is a *minimum delay threshold* before the callback moves to the Task Queue[cite: 1, 2]. Call Stack availability determines final timing[cite: 1, 2].
6. **❌ "Closures cause permanent memory leaks."**[cite: 1, 2]
   * **Reality:** Closures keep reachable variables alive[cite: 1, 2]. Memory leaks occur only when references are retained indefinitely without garbage collection reachability cleanup[cite: 1, 2].

---

# 2️⃣3️⃣ Why This Foundation Powers React ⚛️[cite: 1, 2]

```text
JAVASCRIPT PRIMITIVE                   REACT PARADIGM
────────────────────                   ──────────────
First-Class Functions          ──►     Function Components (`function Card() {}`)[cite: 1]
Closures                       ──►     Hooks & Stale States (`useState`, `useEffect`)[cite: 1]
Array `.map()`                 ──►     Dynamic JSX List Rendering (`items.map(...)`)[cite: 1]
Object Reference Identity      ──►     State Immutability & Re-render detection[cite: 1]
Higher-Order Functions         ──►     Props-as-Functions & Custom Hooks[cite: 1]
Async / Promises               ──►     Data Fetching & React Suspense[cite: 1]
```[cite: 1]

* **Components are Functions:** UI is computed by invoking functional components[cite: 1].
* **Hooks depend on Closures:** `useState` relies on closures to persist state variables across re-renders[cite: 1].
* **Immutability relies on Reference Equality:** React compares object references (`prev === next`) to skip unneeded renders[cite: 1].

---

# 🏁 Next Steps Roadmap[cite: 1, 2]

You now possess a complete first-principles model of:
`Execution Context → Scope → Declarations & TDZ → Closures → First-Class Functions → Async Runtime → Event Loop → Promises → async/await → this`[cite: 1, 2]

```text
🔥 NEXT DEEP CHAPTER:
1. Object Prototypes & [[Prototype]] Chain[cite: 1, 2]
2. ES6 Classes & Inheritance Mechanics[cite: 1, 2]
3. Modules (ESM vs CommonJS) & Bundling Internals[cite: 1, 2]
4. V8 Memory Management & Garbage Collection Mechanics (Mark-and-Sweep)[cite: 1, 2]
```[cite: 1, 2]

