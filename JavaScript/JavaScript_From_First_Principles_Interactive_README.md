# ⚡ JavaScript — The Story of How Everything Happens

> ## 🧠 Don't memorize JavaScript.
> Follow the problems.
> Watch JavaScript solve them.
> By the end, the language stops looking like random features and starts looking like one connected machine.

---

# 🗺️ The Journey

```text
🌍 "I wrote some JavaScript..."
          │
          ▼
🧠 Who executes it?
          │
          ▼
📦 Execution Context
          │
          ▼
📚 Call Stack
          │
          ▼
👻 Hoisting
          │
          ▼
🏠 Scope + Lexical Environment
          │
          ▼
🔒 Closures
          │
          ▼
🎁 Functions as Values
          │
          ▼
📞 Callbacks
          │
          ▼
🌐 Browser / Node Runtime
          │
          ▼
🔄 Event Loop
          │
          ▼
🤝 Promises
          │
          ▼
⏳ async / await
          │
          ▼
🧭 `this`
          │
          ▼
🚀 React is waiting here
```

---

# 1️⃣ The story starts with one innocent file

```js
var x = 10;

function greet() {
    console.log("Hello");
}

greet();
```

You press **Run**.

But...

> 🤔 Who is actually running this?

Not the `.js` file.

Not the browser alone.

Not V8 alone.

The answer is:

```text
┌─────────────────────────────────────┐
│        JAVASCRIPT RUNTIME           │
│                                     │
│  ┌───────────────────────────────┐  │
│  │       JS ENGINE 🧠            │  │
│  │                               │  │
│  │ Parse → Compile → Execute     │  │
│  │ Memory → GC → Call Stack      │  │
│  └───────────────────────────────┘  │
│                                     │
│  + Host APIs 🌐                     │
│    timers, DOM, fetch, events...    │
└─────────────────────────────────────┘
```

### 🌐 In a browser

The browser gives JavaScript extra powers:

```text
DOM
Timers
fetch
Storage
Events
Location
...
```

The JavaScript engine executes JavaScript.

The **host environment** provides APIs around it.

> 🏷️ Official-ish terminology:
>
> - JavaScript is specified by **ECMAScript**
> - V8 is a **JavaScript engine**
> - Chrome is a **browser host/runtime environment**
> - Node.js is another **JavaScript runtime**, with different host APIs

---

# 2️⃣ Before execution: JavaScript must understand your code

You write:

```js
const total = 10 + 20;
```

The engine cannot directly execute English-like source text.

So conceptually:

```text
Source Code
    │
    ▼
🔤 Tokens
    │
    ▼
🌳 Parser
    │
    ▼
AST — Abstract Syntax Tree
    │
    ▼
⚙️ Execution / compilation pipeline
    │
    ▼
🖥️ Machine-level execution
```

Visual:

```text
        =
       / \
   total   +
          / \
        10  20
```

## 🤔 Is JavaScript interpreted or compiled?

Old answer:

```text
JavaScript = interpreted
```

Modern answer:

```text
❌ Too simple.
```

Modern engines use interpretation, compilation and optimization strategies together.

A useful model:

```text
Parse
  ↓
Start executing
  ↓
🔥 Detect frequently executed code
  ↓
Optimize / compile it
  ↓
Execute faster
  ↓
Assumption changed?
  ↓
Possibly deoptimize
```

This is the world of **JIT — Just-In-Time compilation**.

---

# 3️⃣ The core question: where does JavaScript keep track of execution? 📦

You learned the most important beginner mental model:

```text
Execution Context
┌─────────────────────────────┐
│ 🧠 Memory                   │
│                             │
│ variables                   │
│ functions                   │
├─────────────────────────────┤
│ ⚡ Code execution           │
│                             │
│ statements are evaluated    │
└─────────────────────────────┘
```

This is a great model.

But let's attach the deeper names.

Your term:

```text
Memory creation phase
```

Maps conceptually to:

```text
Creation / initialization
Declaration processing
Binding instantiation
```

Your term:

```text
Code execution phase
```

Maps conceptually to:

```text
Execution / evaluation
```

⚠️ Important:

The JavaScript specification does **not literally require engines to implement a physical "memory box" and "code box"**.

But as a learning model?

🔥 Extremely useful.

---

# 4️⃣ The first execution context: Global Execution Context 🌍

Imagine this:

```js
var x = 10;

function a() {
    console.log("A");
}

a();
```

Before ordinary execution proceeds, the global-level environment is prepared.

Conceptually:

```text
GLOBAL EXECUTION CONTEXT 🌍

┌─────────────────────────────────────┐
│ 🧠 BINDINGS / MEMORY                │
│                                     │
│ x → undefined                       │
│ a → function object                 │
│                                     │
├─────────────────────────────────────┤
│ ⚡ EXECUTION                        │
│                                     │
│ var x = 10                          │
│ function a() {}                     │
│ a()                                 │
└─────────────────────────────────────┘
```

Then execution happens.

```text
x → 10
```

Then:

```js
a();
```

A new execution context is created for that function.

🔥 This is the beginning of your entire JavaScript mental model.

---

# 5️⃣ Functions create their own execution worlds 🪆

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
```

What happens?

```text
Global Execution Context
        │
        ▼
       a()
        │
        ▼
Function Execution Context
        │
        ├── x = undefined
        │
        └── x = 20
        │
        ▼
      finishes
        │
        ▼
       b()
        │
        ▼
Function Execution Context
        │
        ├── x = undefined
        │
        └── x = 100
        │
        ▼
      finishes
        │
        ▼
Global continues
```

Output:

```text
20
100
10
```

Each invocation gets its own execution state.

---

# 6️⃣ But who remembers which function is currently running? 📚

Enter:

# 🥞 THE CALL STACK

```text
Run program

┌─────────────────┐
│ Global Context  │
└─────────────────┘

Call a()

┌─────────────────┐
│ Function a      │ ← currently executing
├─────────────────┤
│ Global Context  │
└─────────────────┘

a() finishes

┌─────────────────┐
│ Global Context  │
└─────────────────┘

Call b()

┌─────────────────┐
│ Function b      │
├─────────────────┤
│ Global Context  │
└─────────────────┘
```

Rule:

```text
📥 Function called  → push context
📤 Function returns → pop context
```

That is why it is called a **stack**.

And when the top-level program finishes:

```text
Global execution finishes
        ↓
stack becomes empty
```

---

# 7️⃣ The first mind-blowing question: "How can I use something before its line?" 👻

```js
console.log(x);

var x = 10;
```

Output:

```text
undefined
```

Why didn't JavaScript crash?

Because the `var` binding was already created before ordinary execution reached:

```js
var x = 10;
```

Conceptual model:

```js
var x;

console.log(x);

x = 10;
```

⚠️ Not a literal source-code rewrite.

This behavior is commonly called:

# 👻 Hoisting

But here's the important correction:

> Hoisting does **not** mean JavaScript physically moves your code to the top.

A better understanding:

```text
Environment/declaration setup happens
        ↓
ordinary execution begins
        ↓
some declarations are already available
```

---

# 8️⃣ Function declaration vs function expression 🧨

## Function declaration / statement

```js
hello();

function hello() {
    console.log("Hello");
}
```

Works.

Because the function declaration is available during environment setup.

---

## Function expression

```js
hello();

var hello = function () {
    console.log("Hello");
};
```

Conceptually:

```text
hello → undefined
```

So calling it before assignment fails.

The variable exists.

The function value has not been assigned yet.

---

## Arrow function

```js
hello();

var hello = () => {
    console.log("Hello");
};
```

Same important idea:

```text
var binding exists
        ↓
value is initially undefined
        ↓
function object is assigned later
```

---

# 9️⃣ `undefined` vs "not defined" 🚨

These are NOT the same.

## Case 1: binding exists

```js
var x;

console.log(x);
```

Output:

```text
undefined
```

Meaning:

```text
x exists
but currently has the value undefined
```

---

## Case 2: no accessible binding

```js
console.log(y);
```

Result:

```text
ReferenceError
```

Conceptually:

```text
Search current scope
        ↓
not found
        ↓
search outer lexical scope
        ↓
not found
        ↓
...
        ↓
not found
        ↓
💥 ReferenceError
```

---

# 🔟 JavaScript is dynamically typed 🧬

Java:

```java
int x = 10;
```

JavaScript:

```js
let x = 10;

x = "hello";

x = true;
```

The binding can refer to values of different types over time.

This does **not** mean JavaScript has no types.

It means type restrictions are not declared the same way as Java.

---

# 1️⃣1️⃣ Scope enters the story 🏠

You tried this:

```js
const globalValue = "🌍";

function outer() {
    console.log(globalValue);
}
```

Works.

But:

```js
function outer() {
    const localValue = "🏠";
}

console.log(localValue);
```

Fails.

So JavaScript needs rules:

> Who can access which binding?

That is:

# 🎯 Scope

---

# 1️⃣2️⃣ Lexical Environment — the family tree of variables 🌳

Example:

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
```

Visual:

```text
Global
┌─────────────┐
│ a = "A"     │
└──────┬──────┘
       │ lexical parent
       ▼
x()
┌─────────────┐
│ b = "B"     │
└──────┬──────┘
       │ lexical parent
       ▼
y()
┌─────────────┐
│ c = "C"     │
└─────────────┘
```

When JavaScript evaluates:

```js
console.log(a);
```

inside `y()`:

```text
y environment
    │
    │ a found? ❌
    ▼
x environment
    │
    │ a found? ❌
    ▼
Global environment
    │
    │ a found? ✅
    ▼
Use it
```

This connected lookup path is the:

# 🔗 Scope Chain

And the relationship is determined by **where code is written**, not where a function is called.

That is:

# 🌳 Lexical scoping

---

# 1️⃣3️⃣ `let` and `const`: "Why do we need them if `var` already exists?" 🤔

Because `var` has behavior that can make code harder to reason about:

```js
var x = 10;
var x = 20; // allowed
```

And:

```js
if (true) {
    var x = 10;
}

console.log(x); // 10
```

So modern JavaScript introduced:

```js
let
const
```

---

# 1️⃣4️⃣ TDZ — the scary zone before initialization ☢️

```js
console.log(x);

let x = 10;
```

You may hear:

```text
"let is not hoisted"
```

That explanation is too shallow.

A better model:

```text
Scope is created
       │
       ▼
let binding exists
       │
       │ 🚫 cannot access yet
       │
       ▼
Temporal Dead Zone
       │
       ▼
let x = 10
       │
       ▼
Binding initialized
       │
       ▼
Can access x ✅
```

Trying to access it before initialization:

```text
ReferenceError:
Cannot access 'x' before initialization
```

That region is the:

# ⏳ Temporal Dead Zone (TDZ)

---

# 1️⃣5️⃣ `const` goes one step further 🔒

```js
const x = 10;
```

`const`:

- must be initialized when declared
- cannot be reassigned

```js
const x = 10;

x = 20; // TypeError
```

But important:

```js
const user = {
    name: "Sagar"
};

user.name = "Changed"; // allowed
```

`const` protects the **binding from reassignment**.

It does not magically freeze an object.

🔥 Future you will connect this directly to React and immutability.

---

# 1️⃣6️⃣ Block scope 🧱

A block:

```js
{
    // code
}
```

Example:

```js
if (true) {
    let x = 10;
    const y = 20;
}

console.log(x); // ReferenceError
```

`let` and `const` are lexical/block scoped.

But:

```js
if (true) {
    var x = 10;
}

console.log(x); // 10
```

`var` ignores ordinary block boundaries.

A useful rule:

```text
var
 └── nearest function scope
     or global variable environment

let / const
 └── nearest lexical scope
     often a block
```

---

# 1️⃣7️⃣ Shadowing 🌑

```js
let x = 10;

{
    let x = 20;

    console.log(x); // 20
}

console.log(x); // 10
```

The inner binding hides the outer binding.

```text
Outer x = 10
       │
       ▼
Inner x = 20
       │
       └── shadows outer x
```

The outer variable is still there.

It is just hidden during lookup inside the inner scope.

---

## `var` behaves differently

```js
var x = 10;

{
    var x = 20;
}

console.log(x); // 20
```

Why?

Because both declarations target the same variable scope.

---

# 1️⃣8️⃣ Illegal shadowing 🚫

This is illegal:

```js
let x = 10;

{
    var x = 20;
}
```

Why?

`var` does not stay inside the block.

Conceptually it targets the surrounding variable environment, creating a conflict with the lexical declaration.

But this works:

```js
let x = 10;

function test() {
    var x = 20;
}
```

Visual:

```text
Global lexical scope
┌─────────────────────────┐
│ let x = 10              │
│                         │
│ function scope          │
│ ┌─────────────────────┐ │
│ │ var x = 20          │ │
│ └─────────────────────┘ │
└─────────────────────────┘
```

New function scope.

No collision.

---

# 1️⃣9️⃣ Closures: "Wait... shouldn't this variable be gone?" 🔥

```js
function outer() {
    const x = 10;

    function inner() {
        console.log(x);
    }

    return inner;
}

const fn = outer();

fn();
```

At first:

```text
outer()
    │
    ├── x = 10
    │
    └── return inner
```

Then:

```text
outer finishes
        ↓
its execution context is no longer executing
```

So naturally you ask:

> 🤔 Then how the hell does `inner()` still access `x`?

Because:

# 🔥 CLOSURE

A useful definition:

> A closure is a function together with access to the lexical environment in which it was created.

Visual:

```text
outer()
┌───────────────────────────────┐
│ x = 10                        │
│                               │
│ inner ───────────────────┐    │
└──────────────────────────┼────┘
                           │
                           ▼
                       returned
                           │
                           ▼
                         fn()
                           │
                           ▼
                       access x
```

The execution context finishing does **not** mean every binding it used instantly disappears.

If reachable code still needs bindings, they can remain reachable.

---

# 2️⃣0️⃣ Closure chains 🪆

```js
function x() {
    const a = "A";

    function y() {
        const b = "B";

        function z() {
            console.log(a, b);
        }

        return z;
    }

    return y;
}
```

`z` can access:

```text
z
│
├── b from y
│
└── a from x
```

Lookup:

```text
z environment
      ↓
y environment
      ↓
x environment
      ↓
global
      ↓
null
```

This is why lexical structure matters so much.

---

# 2️⃣1️⃣ Closures + Garbage Collection ♻️

JavaScript has automatic memory management.

A useful mental model:

```text
Reachable from running program?
        │
   ┌────┴────┐
   │         │
  YES       NO
   │         │
   ▼         ▼
Keep       eligible for GC
```

If a returned function still references outer state:

```js
function create() {
    const needed = 10;

    return function () {
        return needed;
    };
}
```

Then `needed` may remain reachable through that closure relationship.

But don't learn:

```text
"Everything in outer function is permanently kept alive" ❌
```

Modern engines can optimize unreachable bindings.

The correct principle:

> Memory that is no longer reachable can become eligible for garbage collection.

---

# 2️⃣2️⃣ The `setTimeout` closure mystery ⏰

You tried:

```js
for (var i = 1; i <= 5; i++) {
    setTimeout(() => {
        console.log(i);
    }, i * 1000);
}
```

You expected:

```text
1
2
3
4
5
```

But got:

```text
6
6
6
6
6
```

WHY?! 🤯

Because `var` gives all callbacks access to the same binding.

```text
Loop starts

i → 1  timer registered
i → 2  timer registered
i → 3  timer registered
i → 4  timer registered
i → 5  timer registered
i → 6  loop finishes
```

Later callbacks execute:

```text
callback 1 → looks up i → 6
callback 2 → looks up i → 6
callback 3 → looks up i → 6
...
```

---

## The `let` solution ✨

```js
for (let i = 1; i <= 5; i++) {
    setTimeout(() => {
        console.log(i);
    }, i * 1000);
}
```

For-loop lexical bindings can provide per-iteration bindings.

Conceptually:

```text
iteration 1 → i = 1
iteration 2 → i = 2
iteration 3 → i = 3
...
```

Each callback closes over the appropriate iteration binding.

---

## The old-school closure solution 🧠

```js
for (var i = 1; i <= 5; i++) {

    function close(value) {
        setTimeout(() => {
            console.log(value);
        }, value * 1000);
    }

    close(i);
}
```

Now:

```text
close(1) → value = 1 → closure
close(2) → value = 2 → closure
close(3) → value = 3 → closure
```

🔥 Same JavaScript.

Different lexical environments.

---

# 2️⃣3️⃣ Closures can hide data 🔐

Problem:

```js
let count = 0;

function increment() {
    count++;
}
```

Anyone can do:

```js
count = 999999;
```

No protection.

Now:

```js
function createCounter() {
    let count = 0;

    return {
        increment() {
            count++;
            return count;
        },

        decrement() {
            count--;
            return count;
        }
    };
}

const counter = createCounter();
```

Now:

```text
Outside world
      │
      ├── can call increment() ✅
      ├── can call decrement() ✅
      └── cannot directly access count ❌
```

That gives you:

```text
🔒 Data hiding
📦 Encapsulation
```

And:

```js
const a = createCounter();
const b = createCounter();
```

creates independent state:

```text
a → closure → count A
b → closure → count B
```

🔥 This is already starting to look like objects and instances.

---

# 2️⃣4️⃣ Constructor functions: before `class` existed 🏗️

JavaScript had constructor functions long before `class` syntax.

```js
function Counter() {
    this.count = 0;

    this.increment = function () {
        this.count++;
    };

    this.decrement = function () {
        this.count--;
    };
}
```

Then:

```js
const c1 = new Counter();
```

Conceptually `new` does something like:

```text
1️⃣ Create a new object
        ↓
2️⃣ Link its prototype
        ↓
3️⃣ Call Counter with `this`
   referring to the new object
        ↓
4️⃣ Return the object
```

This:

```js
this.count = 0;
```

puts state on the new object.

⚠️ Later you should improve this model with:

```text
prototype
shared methods
[[Prototype]]
prototype chain
```

Because putting methods directly on every instance can duplicate function objects.

---

# 2️⃣5️⃣ Functions are not just "things you call" 🎁

JavaScript functions are values.

You can:

```js
const greet = function () {};
```

Store them.

```js
function run(fn) {
    fn();
}
```

Pass them.

```js
function create() {
    return function () {};
}
```

Return them.

This ability makes functions:

# 🥇 First-class values

or:

# 🥇 First-class citizens

---

# 2️⃣6️⃣ Callback: "Here, take my function. Call it later." 📞

```js
function process(callback) {
    // do something
    callback();
}
```

You give a function to another function.

The receiving function controls **when** to invoke it.

That is a:

# 📞 Callback

This becomes massive in asynchronous programming.

---

# 2️⃣7️⃣ Higher-order functions 🧠

A function that:

```text
takes a function
        OR
returns a function
```

is called a:

# 🏗️ Higher-order function

Example:

```js
function calculate(array, operation) {
    const result = [];

    for (const value of array) {
        result.push(operation(value));
    }

    return result;
}
```

Usage:

```js
calculate([1, 2, 3], x => x * 2);
```

Why is this powerful?

Because:

```text
What stays same?
→ looping logic

What changes?
→ operation
```

So we separate:

```text
Generic mechanism
+
Custom behavior
```

This is the foundation behind:

```js
map
filter
reduce
```

---

# 2️⃣8️⃣ `Array.prototype` — why can every array use `.map()`? 🧬

You experimented with:

```js
Array.prototype.calculate = function (logic) {
    // ...
};
```

Then:

```js
[1, 2, 3].calculate(...)
```

works.

Why?

Because arrays inherit behavior through the prototype chain.

We will go insanely deep into that later.

For now:

```text
array
   │
   ▼
Array.prototype
   │
   ├── map
   ├── filter
   ├── reduce
   └── your custom method
```

---

# 2️⃣9️⃣ `map()` transforms 🧪

```js
const numbers = [1, 2, 3];

const doubled = numbers.map(x => x * 2);
```

Visual:

```text
[1, 2, 3]
    │
    │ x * 2
    ▼
[2, 4, 6]
```

`map()` conceptually:

```text
Input array
      ↓
Apply callback to each element
      ↓
New array
```

---

# 3️⃣0️⃣ `filter()` selects 🔎

```js
const numbers = [1, 2, 3, 4];

const even = numbers.filter(x => x % 2 === 0);
```

Visual:

```text
1 ❌
2 ✅
3 ❌
4 ✅

↓

[2, 4]
```

The callback decides:

```text
true  → keep
false → remove from resulting array
```

---

# 3️⃣1️⃣ `reduce()` combines 🧱

```js
const numbers = [1, 2, 3];

const total = numbers.reduce(
    (accumulator, current) => accumulator + current,
    0
);
```

Visual:

```text
Start

acc = 0

0 + 1 → 1
1 + 2 → 3
3 + 3 → 6

Result = 6
```

Pattern:

```text
Many values
    ↓
Repeated accumulation
    ↓
One result
```

That result can be:

```text
number
object
array
map-like structure
anything
```

`reduce()` is far more powerful than "sum".

---

# 3️⃣2️⃣ Callback chaining gets ugly 😭

Imagine e-commerce:

```text
Create Order
     ↓
Make Payment
     ↓
Show Order
     ↓
Update Wallet
```

With callbacks:

```js
createOrder(cart, function () {
    makePayment(function () {
        showOrder(function () {
            updateWallet();
        });
    });
});
```

Visual:

```text
createOrder(
    └── makePayment(
          └── showOrder(
                └── updateWallet()
          )
    )
)
```

# 😭 CALLBACK HELL

Also called:

# 🏜️ Pyramid of Doom

The problem isn't just indentation.

There is another problem:

---

# 3️⃣3️⃣ Inversion of Control 🎮

You give your function away:

```js
createOrder(cart, proceedToPayment);
```

Now:

```text
createOrder controls:
WHEN proceedToPayment runs
HOW MANY TIMES it runs
WHETHER it runs
```

You gave control away.

That is:

# 🔄 Inversion of Control

Promises change the relationship.

---

# 3️⃣4️⃣ Promises: "Don't take my callback. Give me a future result." 🤝

Instead of:

```js
fetchData(callback);
```

You get:

```js
const promise = fetchData();
```

Now YOU decide what to attach:

```js
promise.then(handleData);
```

Visual:

```text
Producer ⚙️
   │
   │ creates promise
   ▼
┌──────────────────────┐
│ Promise              │
│                      │
│ state: pending       │
│ result: empty        │
└──────────┬───────────┘
           │
           │ operation finishes
           ▼
      fulfilled / rejected
           │
           ▼
consumer continuation runs
```

Promise states:

```text
⏳ pending
   │
   ├── ✅ fulfilled
   │
   └── ❌ rejected
```

Once settled, a promise does not go back to pending and settle again.

---

# 3️⃣5️⃣ `fetch()` and the live promise object 🌐

```js
const user = fetch("/api/user");

console.log(user);
```

JavaScript does NOT wait for the network.

You get a promise immediately.

Conceptually:

```text
fetch()
   │
   ├── starts/coordinates async work
   │
   ▼
Promise returned immediately
state: pending
   │
   │ network work continues outside
   ▼
later
   │
   ▼
fulfilled / rejected
```

This is one of the biggest JavaScript principles:

# 🚫 JavaScript does not sit around waiting for slow external work.

---

# 3️⃣6️⃣ But WHERE does async work happen? 🌐

JavaScript engine:

```text
┌───────────────────┐
│ JS ENGINE 🧠      │
│                   │
│ Call Stack 📚     │
│ Memory / GC ♻️    │
└─────────┬─────────┘
          │
          │ host APIs
          ▼
┌───────────────────┐
│ BROWSER 🌐        │
│                   │
│ Timers ⏰         │
│ DOM 🖱️           │
│ Fetch 🌍         │
│ Events 🎯         │
└───────────────────┘
```

Example:

```js
console.log("start");

setTimeout(() => {
    console.log("timer");
}, 2000);

console.log("end");
```

Output:

```text
start
end
timer
```

The timer callback does NOT teleport directly into the call stack.

There is a scheduling system.

---

# 3️⃣7️⃣ Event loop — the traffic controller 🚦

Conceptually:

```text
                 Browser / Host
                      │
                      │ timer finished
                      ▼
                 TASK QUEUE 📬
                      │
                      │
                      ▼
JS ENGINE 🧠     EVENT LOOP 🚦
┌──────────────┐      │
│ Call Stack 📚│ ◄────┘
└──────────────┘
```

But the event loop does NOT interrupt running synchronous JavaScript and randomly push a callback on top.

If the stack is busy:

```text
Call Stack
┌───────────────┐
│ running fn()  │ ← busy
├───────────────┤
│ global work   │
└───────────────┘
```

Ready callbacks wait.

When execution reaches an appropriate scheduling point and the stack is clear, queued work can continue.

Your core model:

```text
Stack busy? ⛔ wait

Stack available?
        ↓
check scheduled work
        ↓
run next eligible work
```

---

# 3️⃣8️⃣ Microtasks: the VIP queue ⚡

Not all async continuations are treated equally.

Promises schedule reaction jobs.

Conceptually:

```text
                ┌──────────────────┐
                │ Call Stack 📚    │
                └────────┬─────────┘
                         │
                    stack clears
                         │
                         ▼
                ┌──────────────────┐
                │ Microtasks ⚡    │  ← priority
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │ Tasks / Macrotask│
                │ Queue 📬         │
                └──────────────────┘
```

Example:

```js
console.log("start");

setTimeout(() => {
    console.log("timer");
}, 0);

Promise.resolve().then(() => {
    console.log("promise");
});

console.log("end");
```

Typical output:

```text
start
end
promise
timer
```

Why?

```text
Promise reaction → microtask ⚡
Timer callback    → task 📬
```

Microtasks are processed before moving on to the next task.

---

## 😈 Microtask starvation

```text
microtask
   ↓ creates
microtask
   ↓ creates
microtask
   ↓ creates
...
```

If microtasks keep continuously adding more microtasks, other work can be delayed.

So yes:

# ⚠️ The task queue can starve.

Mutation observers are another browser feature associated with microtask scheduling.

---

# 3️⃣9️⃣ Browser can do many things while JS executes one main stack 🧠🌐

This distinction is gold:

```text
JavaScript execution:
mostly one main call stack at a time

Browser:
can coordinate timers
networking
rendering
events
other platform work
```

So asynchronous JavaScript does NOT mean:

```text
"JavaScript magically executes two functions on the same call stack simultaneously."
```

It means slow/external work can progress outside that synchronous execution flow, and continuations are scheduled later.

---

# 4️⃣0️⃣ Promise chaining ⛓️

Instead of:

```js
step1(() => {
    step2(() => {
        step3(() => {});
    });
});
```

You can do:

```js
step1()
    .then(step2)
    .then(step3)
    .catch(handleError);
```

Visual:

```text
Promise 1
   │ result
   ▼
.then()
   │ result / next promise
   ▼
.then()
   │
   ▼
.then()
```

Each `.then()` returns a new promise.

That is what allows the chain to continue.

---

# 4️⃣1️⃣ Creating your own Promise 🏭

There are two sides:

```text
🏭 Producer
creates and settles the promise

🙋 Consumer
attaches reactions
```

Example:

```js
function getData() {
    return new Promise((resolve, reject) => {

        const success = true;

        if (success) {
            resolve("Data");
        } else {
            reject(new Error("Something failed"));
        }
    });
}
```

Then:

```js
getData()
    .then(data => {
        console.log(data);
    })
    .catch(error => {
        console.error(error);
    });
```

`resolve` and `reject` are functions supplied to the executor.

You call them to settle the promise.

---

# 4️⃣2️⃣ Promise APIs — choose the behavior you actually want 🧰

## `Promise.all()` 🏁

```js
await Promise.all([
    getUser(),
    getOrders(),
    getRecommendations()
]);
```

```text
all fulfill
   ↓
array of values
```

If one rejects:

```text
❌ fail fast
```

It settles based on rejection without waiting for all results to become fulfilled.

---

## `Promise.allSettled()` 📋

```js
await Promise.allSettled([
    task1(),
    task2(),
    task3()
]);
```

You want:

```text
"I want every outcome."
```

Result includes fulfillment/rejection status.

---

## `Promise.race()` 🏎️

```text
First promise to settle wins.
```

That means:

```text
first fulfilled → fulfill
first rejected  → reject
```

---

## `Promise.any()` 🥇

```text
First promise to fulfill wins.
```

Rejections are ignored until:

```text
ALL promises reject
```

Then the returned promise rejects with an `AggregateError`.

---

# 4️⃣3️⃣ `async`: "This function always speaks Promise" 🗣️

```js
async function getUser() {
    return "Sagar";
}
```

Conceptually:

```text
Return normal value
        ↓
async function
        ↓
Promise fulfilled with that value
```

So:

```js
getUser().then(console.log);
```

works.

If you return a promise, the async function adopts that result according to promise resolution behavior.

---

# 4️⃣4️⃣ `await`: the illusion of synchronous code ⏳✨

```js
async function load() {
    console.log("1");

    const response = await fetch("/api/user");

    console.log("2");
}
```

At:

```js
await fetch(...)
```

Do NOT think:

```text
JavaScript thread freezes ❌
```

Think:

```text
async function begins
        ↓
reaches await
        ↓
promise not settled?
        ↓
pause/suspend this async function's continuation
        ↓
main execution can continue
        ↓
promise settles
        ↓
continuation scheduled as microtask
        ↓
async function resumes
```

That is why:

# 🔥 `await` suspends the async function; it does not synchronously block the entire JavaScript runtime while waiting.

---

# 4️⃣5️⃣ Two awaits means sequential dependency unless you start both first 🧠

```js
const a = await getA();
const b = await getB();
```

Here `b` doesn't even start until `getA()` has completed if the call to `getB()` occurs after the first await.

But:

```js
const promiseA = getA();
const promiseB = getB();

const [a, b] = await Promise.all([
    promiseA,
    promiseB
]);
```

Now both operations can be initiated before awaiting their results.

🔥 This distinction:

```text
concurrency ≠ parallelism
```

will matter later.

---

# 4️⃣6️⃣ `fetch()` has TWO async layers 🌊

```js
const response = await fetch("/api/user");
```

`fetch()` returns a promise.

Once resolved, you receive a `Response`.

But:

```js
const data = await response.json();
```

also returns a promise.

Why?

Because the response body is handled asynchronously.

Flow:

```text
fetch()
  │
  ▼
Promise<Response>
  │
  ▼
Response
  │
  ▼
response.json()
  │
  ▼
Promise<parsed data>
  │
  ▼
JavaScript value
```

---

# 4️⃣7️⃣ Error handling: `.catch()` vs `try/catch` 🧯

Promise style:

```js
getData()
    .then(handleData)
    .catch(handleError);
```

Async/await style:

```js
try {
    const data = await getData();
} catch (error) {
    console.error(error);
}
```

Both connect to promise rejection behavior.

---

# 4️⃣8️⃣ `this`: JavaScript's favorite plot twist 🧭

You discovered that `this` depends heavily on context and invocation.

The master question for a **regular function**:

> 🧠 HOW WAS THE FUNCTION CALLED?

---

## Global context

In a classic browser script, top-level `this` behavior can correspond to the global object, but this changes with modules and environments.

Do not memorize:

```text
"global this ALWAYS equals window" ❌
```

Better:

```text
Browser classic script → commonly window/global object
Browser module → top-level this is undefined
Node CommonJS / ES modules → different host/module semantics
```

---

## Plain function call

```js
function a() {
    console.log(this);
}

a();
```

Strictness matters.

```text
strict mode
→ this = undefined

non-strict regular call
→ this substitution behavior can provide global object
```

---

## Method call

```js
const user = {
    name: "Sagar",

    greet() {
        console.log(this.name);
    }
};

user.greet();
```

Invocation:

```text
user.greet()
^^^^
```

So:

```text
this → user
```

---

## The dangerous extraction 💣

```js
const greet = user.greet;

greet();
```

Now it is no longer called as:

```text
user.greet()
```

It is:

```text
greet()
```

🔥 The call site changed.

So regular-function `this` changes too.

---

# 4️⃣9️⃣ `call`, `apply`, `bind` 🎯

## `call`

```js
function greet() {
    console.log(this.name);
}

greet.call({ name: "Sagar" });
```

Immediate invocation with chosen `this`.

---

## `apply`

Similar idea, but arguments are supplied differently.

```js
greet.apply(
    { name: "Sagar" },
    []
);
```

---

## `bind`

```js
const bound = greet.bind({
    name: "Sagar"
});
```

Does not immediately call the function.

It creates a new function with `this` bound.

Later:

```js
bound();
```

---

# 5️⃣0️⃣ Arrow functions: "I don't create my own `this`" 🏹

```js
const obj = {
    name: "Sagar",

    arrow: () => {
        console.log(this);
    }
};
```

Arrow functions do not determine `this` from their own invocation the same way regular functions do.

They use lexical `this` from the surrounding context.

Mental model:

```text
Regular function:
"How was I called?" 🤔

Arrow function:
"Where was I created?" 🧬
```

---

# 5️⃣1️⃣ DOM event listeners and `this` 🖱️

With a normal listener:

```js
button.addEventListener("click", function () {
    console.log(this);
});
```

The callback invocation can receive an element-related `this` according to the event listener callback rules.

With an arrow:

```js
button.addEventListener("click", () => {
    console.log(this);
});
```

The arrow uses lexical `this` instead.

So:

```text
Normal function → invocation-based `this`

Arrow function → lexical `this`
```

---

# 🧠 The BIG visualization

Everything you learned is one system:

```text
┌─────────────────────────────────────────────────────┐
│                   YOUR JS CODE                      │
└────────────────────────┬────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────┐
│              JAVASCRIPT RUNTIME 🌐                  │
│                                                     │
│   ┌─────────────────────────────────────────────┐   │
│   │ JS ENGINE 🧠                                │   │
│   │                                             │   │
│   │ Parse → AST → Execute / Optimize            │   │
│   │                                             │   │
│   │ Memory / bindings                           │   │
│   │ Execution Contexts 📦                       │   │
│   │ Call Stack 📚                               │   │
│   │ Garbage Collection ♻️                       │   │
│   └──────────────────────┬──────────────────────┘   │
│                          │                          │
│                 synchronous code                    │
│                          │                          │
│   ┌──────────────────────▼──────────────────────┐   │
│   │ HOST ENVIRONMENT APIs 🌐                    │   │
│   │ timers | DOM | fetch | events | storage     │   │
│   └──────────────────────┬──────────────────────┘   │
│                          │                          │
│            ┌─────────────┴─────────────┐            │
│            ▼                           ▼            │
│      Microtasks ⚡                Tasks 📬           │
│      Promise reactions            timers/events      │
│            │                           │            │
│            └─────────────┬─────────────┘            │
│                          ▼                          │
│                    Event Loop 🚦                    │
│                          │                          │
│                          ▼                          │
│                     Call Stack 📚                   │
└─────────────────────────────────────────────────────┘
```

---

# 🚨 Misconceptions you should NEVER carry forward

## ❌ "JavaScript executes literally one source line at a time"

Better:

> JavaScript evaluates code according to language semantics using execution contexts and an execution stack. "One line at a time" is a useful beginner approximation.

---

## ❌ "Hoisting moves code upward"

Better:

> Hoisting describes observable behavior caused by environment/declaration setup before ordinary execution.

---

## ❌ "`let` and `const` don't exist before their line"

Better:

> Their bindings exist in the lexical environment, but access before initialization is blocked by the TDZ.

---

## ❌ "`setTimeout(fn, 1000)` means exactly after one second"

Better:

```text
Minimum delay threshold
        +
event loop scheduling
        +
main-thread availability
        =
actual callback timing
```

---

## ❌ "`fetch()` rejects for HTTP 404/500 automatically"

Better:

> A network-level failure and an HTTP error response are different things. Check `response.ok` / status when needed.

---

## ❌ "`async/await` blocks JavaScript"

Better:

> `await` suspends the continuation of that async function while other JavaScript work can continue.

---

## ❌ "Closures are memory leaks"

Better:

> Closures can keep reachable state alive. A memory leak occurs when unnecessary objects remain reachable.

---

# 🧩 What you have NOT deeply learned yet

And this is important: don't randomly jump around.

The natural continuation is:

```text
1️⃣ Objects from first principles 🧱
        ↓
2️⃣ Primitive vs reference values 🔗
        ↓
3️⃣ Object mutation and identity 🧬
        ↓
4️⃣ Prototype chain 🧬
        ↓
5️⃣ Constructor functions + `new` 🏗️
        ↓
6️⃣ Classes 🎭
        ↓
7️⃣ Deep `this` with prototypes/classes 🧭
        ↓
8️⃣ Type coercion + equality 🧪
        ↓
9️⃣ Arrays + iterables + iterators 🔄
        ↓
🔟 Destructuring + spread/rest 🎁
        ↓
1️⃣1️⃣ Errors deeply 🧯
        ↓
1️⃣2️⃣ Modules 📦
        ↓
1️⃣3️⃣ Advanced Promises / thenables ⚡
        ↓
1️⃣4️⃣ Generators + async iterators 🌊
        ↓
1️⃣5️⃣ Memory + GC + performance ♻️
        ↓
1️⃣6️⃣ JavaScript patterns 🏛️
        ↓
⚛️ REACT
```

---

# ⚛️ Heads-up: why all of this matters for React

React is not a separate universe.

React is standing on top of this JavaScript.

## Functions → Components

```jsx
function UserCard() {
    return <h1>Hello</h1>;
}
```

You already know:

```text
functions are values
functions can be passed
functions can close over state
```

---

## Closures → Event handlers and stale values

```jsx
function Counter() {
    const [count, setCount] = useState(0);

    function handleClick() {
        console.log(count);
    }
}
```

Every render creates values and functions.

Functions can capture values from the render in which they were created.

This is the doorway to understanding:

```text
stale closures
useEffect dependencies
useCallback
event handlers
```

---

## `map()` → UI rendering

You learned:

```js
users.map(transform)
```

React:

```jsx
users.map(user => (
    <UserCard
        key={user.id}
        user={user}
    />
));
```

Same functional foundation.

Different output.

```text
Data
 ↓
map
 ↓
UI
```

---

## Objects + references → React state

Future JavaScript knowledge:

```text
object identity
mutation
shallow copies
spread
immutability
```

explains why React state is commonly updated like:

```js
setUser(previous => ({
    ...previous,
    name: "New Name"
}));
```

---

## Callbacks → Props

```jsx
<Child onSave={handleSave} />
```

You are passing a function as a value.

Later:

```js
onSave(data);
```

Callback.

You already know the foundation.

---

## Promises + async/await → Data fetching

```js
const response = await fetch("/api/users");
const data = await response.json();
```

Nothing magical.

You already know:

```text
fetch
↓
Promise
↓
await
↓
Response
↓
response.json()
↓
Promise
↓
data
```

React simply builds application behavior around these primitives.

---

# 🏁 The final mental model

You started here:

```text
"I wrote JavaScript.
How does it execute?"
```

Now see this:

```text
SOURCE CODE 📝
      ↓
PARSE 🌳
      ↓
EXECUTION CONTEXT 📦
      ↓
CALL STACK 📚
      ↓
BINDINGS 🧠
      ↓
SCOPE 🏠
      ↓
LEXICAL ENVIRONMENT 🌳
      ↓
FUNCTIONS 🎁
      ↓
FIRST-CLASS FUNCTIONS 🥇
      ↓
CLOSURES 🔒
      ↓
CALLBACKS 📞
      ↓
HOST APIs 🌐
      ↓
EVENT LOOP 🚦
      ↓
MICROTASKS ⚡ + TASKS 📬
      ↓
PROMISES 🤝
      ↓
ASYNC / AWAIT ⏳
      ↓
OBJECTS + REFERENCES 🧱   ← NEXT BIG CHAPTER
      ↓
PROTOTYPES 🧬
      ↓
CLASSES 🎭
      ↓
MODULES 📦
      ↓
REACT ⚛️
```

---

# ❤️ The real beauty of JavaScript

At first JavaScript feels chaotic:

```text
Why undefined? 😵
Why closure? 😵
Why callback? 😵
Why Promise? 😵
Why event loop? 😵
Why is `this` changing? 😭
```

But then you follow the chain:

```text
Problem
   ↓
Limitation
   ↓
New idea
   ↓
New power
   ↓
New problem
   ↓
Next abstraction
```

And suddenly:

```text
Execution Context
      ↓
Scope
      ↓
Closure
      ↓
Callback
      ↓
Async Runtime
      ↓
Promise
      ↓
async / await
```

is not 8 random topics.

It is one story.

# 🚀 JavaScript is basically a language where understanding "what happens next?" changes everything.

---

> ## 🎯 Current checkpoint
>
> You have built a strong foundation around:
>
> **execution → scope → lexical environments → closures → functions → callbacks → async runtime → event loop → promises → async/await → `this`**
>
> ## 🔥 Next:
>
> **Objects from first principles → references → mutation → prototypes → constructor functions → `new` → classes**
>
> That's where JavaScript's other giant rabbit hole begins. 🕳️🐇
