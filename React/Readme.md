# React --- From First Principles to Internals

> A deep, first-principles README for understanding React as a system
> rather than memorizing its APIs.

------------------------------------------------------------------------

## 0. How to read this README

React becomes much easier when learned in this order:

``` text
PROBLEM
   ↓
WHY AN ABSTRACTION IS NEEDED
   ↓
MENTAL MODEL
   ↓
API
   ↓
RUNTIME BEHAVIOR
   ↓
DATA STRUCTURES
   ↓
ALGORITHMS
   ↓
BROWSER
```

The goal is not merely to know:

``` js
useState(...)
useEffect(...)
useCallback(...)
```

The goal is to be able to answer:

-   What problem does this solve?
-   What does React need to remember?
-   What happens when state changes?
-   What is a render?
-   What is reconciliation?
-   What is Fiber?
-   When does the real DOM change?
-   When does the browser paint?
-   Why do Hooks have rules?
-   Why do keys matter?
-   Why does Context exist?
-   Why does Redux exist?
-   What is React responsible for and what is the browser responsible
    for?

------------------------------------------------------------------------

# 1. The browser underneath React

React runs **inside JavaScript**, and JavaScript runs inside a browser
environment.

A simplified browser model:

``` text
┌─────────────────────────────────────────────────────────────┐
│                         BROWSER                             │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    MAIN THREAD                       │  │
│  │                                                       │  │
│  │  JavaScript Engine                                   │  │
│  │      │                                                │  │
│  │      ├── Your JavaScript                              │  │
│  │      ├── React                                        │  │
│  │      └── ReactDOM                                     │  │
│  │                                                       │  │
│  │  Browser DOM / browser APIs                           │  │
│  │                                                       │  │
│  └──────────────────────────┬────────────────────────────┘  │
│                             │                               │
│                             ▼                               │
│                    Browser DOM                              │
│                             │                               │
│                             ▼                               │
│                Rendering Pipeline                           │
│                             │                               │
│                    Style → Layout                           │
│                             │                               │
│                             ▼                               │
│                       Paint / Composite                     │
│                             │                               │
│                             ▼                               │
│                           SCREEN                            │
└─────────────────────────────────────────────────────────────┘
```

React is **not the browser**.

React is JavaScript code that helps manage UI state and UI updates.

------------------------------------------------------------------------

# 2. Why React exists

Before React, JavaScript already had the DOM:

``` js
document.querySelector("#count").textContent = count;
```

So React was not invented because JavaScript could not modify HTML.

The actual problem is **state → UI synchronization**.

Imagine:

``` text
Application state
       │
       ├──────────────┐
       ▼              ▼
   Counter text    Header text
       │              │
       ▼              ▼
    Sidebar        Cart badge
       │              │
       └──────┬───────┘
              ▼
        many DOM updates
```

If state changes frequently, manually maintaining every affected DOM
location becomes difficult.

React introduces a declarative model:

``` text
        STATE
          │
          ▼
     UI = f(state)
          │
          ▼
       React
          │
          ▼
       DOM
```

Instead of telling React every DOM operation:

``` text
"Find this element."
"Change this text."
"Hide this node."
"Show that node."
"Update this attribute."
```

you describe what the UI should look like for the current state.

------------------------------------------------------------------------

# 3. Imperative vs declarative UI

## Imperative

``` js
count++;

document.querySelector("#counter").textContent = count;

if (count > 10) {
    document.querySelector("#warning").style.display = "block";
}
```

You describe **how to change the UI**.

## Declarative

``` jsx
return (
    <>
        <h1>{count}</h1>
        {count > 10 && <Warning />}
    </>
);
```

You describe **what the UI should be for this state**.

Mental model:

``` text
Imperative
───────────
State change
   ↓
Developer manually calculates DOM mutations


Declarative
───────────
State change
   ↓
New UI description
   ↓
React calculates required DOM changes
```

------------------------------------------------------------------------

# 4. Where React sits

A typical React application:

``` text
index.html
    │
    ▼
<script type="module" src="/src/main.jsx">
    │
    ▼
main.jsx
    │
    ├── React
    ├── ReactDOM
    └── App
         │
         ▼
       React
         │
         ▼
      ReactDOM
         │
         ▼
    Browser DOM
```

Typical entry:

``` jsx
import { createRoot } from "react-dom/client";
import App from "./App.jsx";

const root = createRoot(document.getElementById("root"));

root.render(<App />);
```

`document.getElementById("root")` is a **real DOM node**.

`createRoot()` tells React that it manages the React application mounted
into that DOM subtree.

------------------------------------------------------------------------

# 5. JSX

JSX is syntax that lets you describe UI inside JavaScript.

``` jsx
const element = <h1>Hello</h1>;
```

The browser does not natively execute JSX syntax.

A build tool transforms JSX into JavaScript.

Conceptually:

``` text
JSX
 │
 ▼
JSX transformation
 │
 ▼
React element creation
 │
 ▼
React representation of UI
```

Historically this looked like:

``` js
React.createElement("h1", null, "Hello");
```

Modern React projects commonly use the automatic JSX runtime, so the
exact generated code can differ.

### JSX expressions

Inside JSX:

``` jsx
<h1>{name}</h1>
```

`{}` means:

> Evaluate this as a JavaScript expression.

Examples:

``` jsx
{user.name}
{2 + 3}
{isLoggedIn ? "Logout" : "Login"}
{getName()}
```

Statements such as `if`, `for`, and `const x = ...` are not directly
expressions inside JSX braces.

------------------------------------------------------------------------

# 6. React elements

A React element is a JavaScript representation describing UI.

Conceptually:

``` js
{
    type: "h1",
    props: {
        children: "Hello"
    }
}
```

For:

``` jsx
<h1>Hello</h1>
```

The important distinction is:

``` text
React element
    ≠
DOM element
```

A React element is a description.

A DOM element is an actual browser object.

``` text
React element
     │
     ▼
React processing
     │
     ▼
ReactDOM
     │
     ▼
actual DOM element
```

------------------------------------------------------------------------

# 7. Host elements vs components

Consider:

``` jsx
<h1>Hello</h1>
```

`"h1"` is a host element type.

Now:

``` jsx
<Counter />
```

`Counter` is a component type.

Conceptually:

``` text
<h1 />
   │
   ▼
host element
   │
   ▼
DOM


<Counter />
   │
   ▼
component
   │
   ▼
Counter()
   │
   ▼
more React elements
```

This distinction becomes extremely important when understanding
rendering and reconciliation.

------------------------------------------------------------------------

# 8. Components

A function component is a JavaScript function used by React to describe
UI.

``` jsx
function Counter() {
    return <h1>0</h1>;
}
```

Three things must not be confused:

``` text
Counter
    =
function value


Counter()
    =
direct JavaScript function invocation


<Counter />
    =
React element describing a Counter component
```

Normally React should control component invocation.

Why?

Because React needs to coordinate:

-   state
-   Hooks
-   identity
-   rendering
-   effects
-   scheduling
-   reconciliation

------------------------------------------------------------------------

# 9. Props

Props are inputs passed from a parent component to a child component.

``` jsx
function User({ name, age }) {
    return <h1>{name} - {age}</h1>;
}

<User name="Sagar" age={22} />
```

Conceptually:

``` text
Parent
  │
  │ props
  ▼
Child
```

Props are data supplied by the parent.

A component should normally treat props as read-only.

------------------------------------------------------------------------

# 10. Props as a React element representation

For:

``` jsx
<User name="Sagar" age={22} />
```

the React representation conceptually contains:

``` js
{
    type: User,
    props: {
        name: "Sagar",
        age: 22
    }
}
```

React later invokes the component with those props.

This connects JSX, React elements, and function parameters:

``` text
<User name="Sagar" />
        │
        ▼
React element
        │
        ▼
type = User
props = { name: "Sagar" }
        │
        ▼
React calls component
        │
        ▼
User({ name: "Sagar" })
```

------------------------------------------------------------------------

# 11. Component composition

Reusable components become powerful through composition.

Instead of making one giant component:

``` jsx
<Card
    title="Product"
    image={image}
    price={price}
    footer={<Button />}
    ...
/>
```

you can compose:

``` jsx
<Card>
    <ProductImage />
    <ProductInfo />
    <AddToCartButton />
</Card>
```

The `children` prop represents nested content:

``` jsx
function Card({ children }) {
    return <div className="card">{children}</div>;
}
```

Mental model:

``` text
Reusable component
        │
        ├── accepts data via props
        ├── accepts behavior via callbacks
        ├── accepts styling/configuration
        └── accepts structure via children
```

Composition is one of React's core design patterns.

------------------------------------------------------------------------

# 12. Generic reusable components

A good reusable component is generic enough to handle different data.

Example:

``` jsx
function InputBox({
    value,
    onChange,
    placeholder,
    className
}) {
    return (
        <input
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            className={className}
        />
    );
}
```

The component owns the reusable UI structure.

The parent supplies:

-   value
-   behavior
-   styling
-   configuration

This separation makes the component reusable.

------------------------------------------------------------------------

# 13. State: the first major React problem

Consider:

``` jsx
function Counter() {
    let count = 0;

    function handleClick() {
        count++;
    }

    return <h1>{count}</h1>;
}
```

Changing `count` does not automatically cause the component to execute
again.

JavaScript variables do not automatically synchronize with the DOM.

This is where React state begins.

------------------------------------------------------------------------

# 14. `useState`

``` jsx
const [count, setCount] = useState(0);
```

The two values mean:

``` text
count
  =
state snapshot for this render


setCount
  =
request/update mechanism for the state
```

The crucial mental model:

``` text
Component function
      │
      │ executes
      ▼
temporary local variables

React
      │
      ▼
persistent state
```

The component function does not own persistent state.

React does.

------------------------------------------------------------------------

# 15. State as a snapshot

Suppose:

``` jsx
function Counter() {
    const [count, setCount] = useState(0);

    function handleClick() {
        console.log(count);

        setCount(count + 1);

        console.log(count);
    }

    return <button onClick={handleClick}>{count}</button>;
}
```

If the current render has:

``` text
count = 0
```

then both reads inside that event handler see:

``` text
0
```

Even after:

``` js
setCount(1);
```

The current render's `count` does not mutate.

React requests another render.

React's official docs describe state as a snapshot: each render receives
its own state value, and setting state triggers another render rather
than changing the current render's variable.

------------------------------------------------------------------------

# 16. Snapshot visualization

``` text
                    TIME →

Render #1             Render #2             Render #3

┌────────────┐       ┌────────────┐       ┌────────────┐
│ count = 0  │       │ count = 1  │       │ count = 2  │
│            │       │            │       │            │
│ handlers   │       │ handlers   │       │ handlers   │
│ see 0      │       │ see 1      │       │ see 2      │
└────────────┘       └────────────┘       └────────────┘
```

Every render produces a new JavaScript execution.

Every render gets a new snapshot.

------------------------------------------------------------------------

# 17. Why state survives function execution

The component:

``` jsx
Counter()
```

finishes.

Its local variables are temporary.

But React maintains persistent information associated with the
component's place in the UI tree.

Conceptually:

``` text
┌───────────────────────┐
│ Component execution   │
│                       │
│ count = 0             │
└───────────┬───────────┘
            │
            │ useState
            ▼
┌───────────────────────┐
│ React's persistent    │
│ component information │
│                       │
│ state = 0             │
└───────────────────────┘
```

On another render:

``` text
React persistent state = 1
             │
             ▼
        useState(...)
             │
             ▼
        count = 1
```

------------------------------------------------------------------------

# 18. Batching

Suppose:

``` jsx
function handleClick() {
    setCount(count + 1);
    setCount(count + 1);
    setCount(count + 1);
}
```

If the snapshot is:

``` text
count = 0
```

then all three expressions calculate:

``` text
0 + 1 = 1
```

Conceptually the queued updates are:

``` text
replace with 1
replace with 1
replace with 1
```

The resulting state is:

``` text
1
```

not 3.

React batches state updates so multiple updates can be processed
together rather than causing a separate visible render for every setter
call.

------------------------------------------------------------------------

# 19. Updater functions

For sequential updates:

``` jsx
setCount(prev => prev + 1);
setCount(prev => prev + 1);
setCount(prev => prev + 1);
```

React can process them conceptually as:

``` text
initial = 0

updater #1:
0 → 1

updater #2:
1 → 2

updater #3:
2 → 3
```

Important:

> `prev` is not a live reference to the current JavaScript variable.

It is the pending state value React passes to the updater while
processing the queued updates.

This is structurally similar to a pipeline:

``` text
0
 ↓
f(x) = x + 1
 ↓
1
 ↓
f(x) = x + 1
 ↓
2
 ↓
f(x) = x + 1
 ↓
3
```

This resembles a `.then()` chain in the sense that each transformation
receives the previous result, but React updater functions are not
Promises.

------------------------------------------------------------------------

# 20. Render, reconciliation, commit

React's rendering pipeline can be understood as:

``` text
┌─────────────────────────────────────────────┐
│                 TRIGGER                     │
│                                             │
│ initial render / state update / parent     │
└─────────────────────┬───────────────────────┘
                      ▼
┌─────────────────────────────────────────────┐
│                  RENDER                     │
│                                             │
│ React calls components                      │
│ creates/calculates new UI representation    │
└─────────────────────┬───────────────────────┘
                      ▼
┌─────────────────────────────────────────────┐
│              RECONCILIATION                 │
│                                             │
│ previous tree vs new tree                   │
│ determine required changes                  │
└─────────────────────┬───────────────────────┘
                      ▼
┌─────────────────────────────────────────────┐
│                  COMMIT                     │
│                                             │
│ renderer applies required changes           │
└─────────────────────┬───────────────────────┘
                      ▼
                 Browser DOM
                      │
                      ▼
              Style/Layout/Paint
```

React's official docs describe the user-facing pipeline as trigger →
render → commit. Rendering does not necessarily mean the DOM changes;
React calculates what is needed and commits only the necessary DOM
operations.

------------------------------------------------------------------------

# 21. Rendering does NOT mean DOM repaint

These are different concepts:

``` text
React render
    ≠
reconciliation
    ≠
commit
    ≠
DOM mutation
    ≠
browser paint
```

React can render a component and ultimately make **no DOM changes** if
the committed result is equivalent.

The browser then performs its own rendering work.

------------------------------------------------------------------------

# 22. Why the Virtual DOM phrase can mislead

"Virtual DOM" is a useful shorthand for React's in-memory representation
of UI, but don't think:

> React compares the virtual DOM directly against the real DOM.

A better model:

``` text
Previous React representation
             │
             │ compare/reconcile
             ▼
New React representation
             │
             ▼
Required changes
             │
             ▼
ReactDOM renderer
             │
             ▼
Real DOM
```

Reconciliation compares React's previous and new representations.

The renderer applies the resulting changes to the target environment.

------------------------------------------------------------------------

# 23. Fiber

Fiber is React's architecture for representing and processing rendering
work.

It is **not simply another name for Virtual DOM**.

Conceptually:

``` text
React element tree
       │
       ▼
Fiber tree
       │
       ├── component information
       ├── props
       ├── state/Hook information
       ├── child
       ├── sibling
       ├── parent/return
       ├── pending work
       └── alternate
```

A Fiber can be thought of as a unit of work / virtual stack frame.

------------------------------------------------------------------------

# 24. Why Fiber was needed

A large UI can contain enormous rendering work:

``` text
App
├── Header
├── Sidebar
├── Main
│   ├── List
│   │   ├── Item
│   │   ├── Item
│   │   ├── Item
│   │   └── ...
│   └── Details
└── Footer
```

A purely synchronous recursive traversal can monopolize the JavaScript
thread.

Fiber represents work in units that React can schedule.

Conceptually:

``` text
Fiber A
   ↓
Fiber B
   ↓
Fiber C
   ↓
yield / continue later
   ↓
Fiber D
   ↓
Fiber E
```

The Fiber architecture was designed around goals including incremental
rendering, pausing/resuming work, prioritizing work, and
abandoning/reusing work.

------------------------------------------------------------------------

# 25. Fiber is not "only repaint changed pixels"

This common explanation is wrong.

React does not control the browser's exact pixel repaint regions.

The responsibilities are:

``` text
React
  ↓
render + reconciliation + commit decisions

ReactDOM
  ↓
DOM operations

Browser
  ↓
style/layout/paint/compositing
```

Fiber primarily changes how React manages its internal rendering work.

------------------------------------------------------------------------

# 26. Hooks

Hooks let function components use React features such as state, effects,
refs, context, and memoization.

Examples:

``` text
useState
useEffect
useRef
useContext
useReducer
useMemo
useCallback
```

A fundamental rule:

> Hooks must be called consistently at the top level of React components
> or custom Hooks.

------------------------------------------------------------------------

# 27. Why Hook order matters

Suppose:

``` jsx
function Component() {
    const [a, setA] = useState(0);
    const [b, setB] = useState(10);
}
```

Conceptually React can associate:

``` text
Hook #1 → a
Hook #2 → b
```

On the next render:

``` text
first useState  → Hook #1
second useState → Hook #2
```

If Hook calls could conditionally appear/disappear, React would lose the
mapping.

Therefore:

``` jsx
if (condition) {
    useState(0); // invalid Hook pattern
}
```

Hooks must remain in a stable order.

------------------------------------------------------------------------

# 28. `useEffect`

Rendering should be a pure calculation:

``` text
props + state
     ↓
JSX
```

But applications also need to synchronize with things outside React:

``` text
WebSocket
API
browser APIs
timers
third-party widgets
subscriptions
```

`useEffect` is for effects caused by rendering that need synchronization
with an external system.

``` jsx
useEffect(() => {
    const connection = connect(roomId);

    return () => {
        connection.disconnect();
    };
}, [roomId]);
```

Mental model:

``` text
render
  ↓
commit DOM
  ↓
Effect setup
  ↓
external system synchronized
```

React's current docs emphasize that Effects are for synchronizing with
external systems and that many computations based only on props/state do
not need an Effect.

------------------------------------------------------------------------

# 29. Effect dependencies

``` jsx
useEffect(() => {
    connect(roomId);
}, [roomId]);
```

The dependency list describes values used by the effect that determine
when the synchronization needs to be re-established.

Conceptually:

``` text
roomId = A
   ↓
setup A

roomId remains A
   ↓
no new synchronization needed

roomId = B
   ↓
cleanup A
   ↓
setup B
```

Do not reduce the concept to:

``` text
[] = once
[x] = when x changes
```

The deeper concept is:

> What external synchronization does this Effect depend on?

------------------------------------------------------------------------

# 30. Effect cleanup

An Effect may return a cleanup function:

``` jsx
useEffect(() => {
    const connection = connect();

    return () => {
        connection.disconnect();
    };
}, []);
```

Conceptually:

``` text
SETUP
  ↓
external resource exists
  ↓
component/effect needs synchronization changed
  ↓
CLEANUP
  ↓
SETUP again if necessary
```

Cleanup prevents leaked subscriptions, timers, connections, and other
external resources.

------------------------------------------------------------------------

# 31. `useRef`

A ref is a persistent mutable container:

``` jsx
const ref = useRef(null);
```

Mental model:

``` text
ref
┌────────────────┐
│ current        │
│      ↓         │
│    value       │
└────────────────┘
```

Changing:

``` js
ref.current = 5;
```

does not request a React render.

This is different from state.

``` text
useState
────────
persistent value
+
setter schedules render


useRef
──────
persistent mutable value
+
mutation does not schedule render
```

Refs are commonly used for:

-   DOM nodes
-   timers
-   mutable values that should survive renders
-   imperative integrations

A ref is a normal JavaScript object whose `.current` can be mutated.
React's docs caution against reading/writing refs during rendering
except for limited initialization patterns.

------------------------------------------------------------------------

# 32. DOM refs

``` jsx
const inputRef = useRef(null);

return <input ref={inputRef} />;
```

After commit:

``` text
inputRef.current
       │
       ▼
actual DOM input
```

React attaches the DOM node to the ref during commit.

Refs should generally be read in event handlers or Effects, not used as
a substitute for render state.

------------------------------------------------------------------------

# 33. `useCallback`

JavaScript functions are objects.

This:

``` js
const a = () => {};
const b = () => {};
```

creates different function objects:

``` text
a !== b
```

A component rendering again recreates functions declared inside it.

``` text
Render #1
   ↓
handleClick = Function A

Render #2
   ↓
handleClick = Function B

A !== B
```

`useCallback` can preserve a function identity while its dependencies
remain the same:

``` jsx
const handleClick = useCallback(() => {
    doSomething(id);
}, [id]);
```

Mental model:

``` text
dependencies same
       ↓
reuse function identity

dependency changed
       ↓
create new function
```

`useCallback` is a performance/identity tool. It is not required for
every function.

------------------------------------------------------------------------

# 34. `useMemo`

`useMemo` caches a calculated value:

``` jsx
const result = useMemo(
    () => expensiveCalculation(data),
    [data]
);
```

Conceptually:

``` text
dependency unchanged
       ↓
reuse cached value

dependency changed
       ↓
calculate again
```

Difference:

``` text
useMemo
───────
memoize VALUE


useCallback
───────────
memoize FUNCTION
```

Neither should be used merely because "React rerenders".

They are optimization tools.

------------------------------------------------------------------------

# 35. `React.memo`

A parent can render:

``` text
Parent
  ↓
Child
```

When the parent renders, the child normally participates in rendering
too.

`React.memo` can allow a child to skip rendering when its relevant props
are unchanged.

Conceptually:

``` text
Parent renders
      ↓
Child receives props
      ↓
compare props
   /       \
same       changed
 │           │
 ▼           ▼
skip       render
```

This is why `React.memo`, `useCallback`, and `useMemo` are often
discussed together:

``` text
React.memo
    +
stable props
    +
useCallback/useMemo
```

But memoization is only useful when it solves an actual performance
problem.

------------------------------------------------------------------------

# 36. Custom Hooks

A custom Hook is a JavaScript function that composes existing Hooks into
reusable stateful logic.

``` jsx
function useOnlineStatus() {
    const [online, setOnline] = useState(false);

    useEffect(() => {
        const connection = connect();

        connection.on("online", () => setOnline(true));
        connection.on("offline", () => setOnline(false));

        return () => connection.disconnect();
    }, []);

    return online;
}
```

Usage:

``` jsx
function Profile() {
    const online = useOnlineStatus();

    return <p>{online ? "Online" : "Offline"}</p>;
}
```

Important:

> Custom Hooks share logic, not state.

If two components call:

``` jsx
useCounter();
```

each component gets its own Hook state.

------------------------------------------------------------------------

# 37. Context

Context solves a data distribution problem.

Without Context:

``` text
App
 ↓ props
Dashboard
 ↓ props
Profile
 ↓ props
Avatar
```

With Context:

``` text
              Provider
                  │
        ┌─────────┼─────────┐
        ▼         ▼         ▼
      A           B         C
                  │
                  ▼
              useContext()
```

Context means:

> Make a value available to components in a subtree without manually
> passing it through every intermediate component.

Context itself is not automatically a complete global state-management
system.

------------------------------------------------------------------------

# 38. Context + state

A common pattern:

``` jsx
const [theme, setTheme] = useState("light");

<ThemeContext value={{ theme, setTheme }}>
    <App />
</ThemeContext>
```

Then:

``` jsx
const { theme, setTheme } = useContext(ThemeContext);
```

This gives components access to shared state and its update mechanism.

A theme system is a good example:

``` text
React state
    ↓
Context
    ↓
set theme
    ↓
<html class="dark">
    ↓
Tailwind/CSS rules
    ↓
browser styles UI
```

React does not need to individually set every background color. CSS
handles the styling after the theme class changes.

------------------------------------------------------------------------

# 39. `useReducer`

`useReducer` is useful when state transitions are more naturally
represented as actions.

``` jsx
const [state, dispatch] = useReducer(reducer, initialState);
```

Mental model:

``` text
current state
     │
     ▼
dispatch(action)
     │
     ▼
reducer(state, action)
     │
     ▼
new state
```

Example:

``` js
function reducer(state, action) {
    switch (action.type) {
        case "increment":
            return { ...state, count: state.count + 1 };

        case "decrement":
            return { ...state, count: state.count - 1 };

        default:
            return state;
    }
}
```

Think:

``` text
useState
────────
"set this state"


useReducer
──────────
"this event happened; calculate the next state"
```

------------------------------------------------------------------------

# 40. Lists and keys

Rendering lists:

``` jsx
products.map(product => (
    <ProductCard
        key={product.id}
        product={product}
    />
))
```

A key provides identity among siblings.

It is not merely a warning suppressor.

Mental model:

``` text
previous children
─────────────────
A
B
C


new children
────────────
A
C
D
```

Keys help React understand:

``` text
A → same item
B → removed
C → same item
D → new item
```

Keys become especially important when list items contain state.

------------------------------------------------------------------------

# 41. Component identity and state preservation

React associates state with a component's position in the render tree.

``` text
App
├── Counter   ← state A
└── Counter   ← state B
```

Although both are the same component function, they occupy different
positions.

State is therefore isolated.

Changing component type or key can cause state to reset.

This is why keys are connected to state preservation, not just list
rendering.

------------------------------------------------------------------------

# 42. Conditional rendering

React supports:

``` jsx
{isLoggedIn && <Dashboard />}
```

``` jsx
{isLoggedIn ? <Dashboard /> : <Login />}
```

and early returns:

``` jsx
if (!user) {
    return <Login />;
}

return <Dashboard />;
```

The deeper question is:

> What component tree exists at each render?

That tree determines identity and state preservation.

------------------------------------------------------------------------

# 43. Forms

## Controlled input

``` jsx
const [email, setEmail] = useState("");

<input
    value={email}
    onChange={e => setEmail(e.target.value)}
/>
```

Mental model:

``` text
input event
    ↓
setState
    ↓
render
    ↓
value prop
    ↓
input
```

React state becomes the source of truth.

## Uncontrolled input

The DOM keeps the current input value and a ref can access it:

``` jsx
const inputRef = useRef(null);

<input ref={inputRef} />
```

The distinction:

``` text
Controlled
──────────
React state → input


Uncontrolled
────────────
DOM → input value
        ↑
       ref
```

------------------------------------------------------------------------

# 44. Events

React event handlers are functions attached through JSX:

``` jsx
<button onClick={handleClick}>
    Add
</button>
```

The broad flow:

``` text
browser interaction
       ↓
React event handling
       ↓
handler
       ↓
state update
       ↓
render
       ↓
commit
```

Common operations:

``` js
event.preventDefault();
event.stopPropagation();
```

Understand event propagation independently from state rendering.

------------------------------------------------------------------------

# 45. Routing

A browser application has a URL:

``` text
/users/42
/products
/cart
/settings
```

A client-side router maps URLs to UI.

Conceptually:

``` text
URL
 │
 ▼
Router
 │
 ▼
route matching
 │
 ├── /products → Products
 ├── /cart     → Cart
 └── /users/:id → User
```

With React Router, common concepts include:

-   BrowserRouter
-   Routes
-   Route
-   Link
-   navigation
-   route params
-   nested routes
-   layouts
-   outlet
-   navigation state

------------------------------------------------------------------------

# 46. Browser routing

`BrowserRouter` uses browser history APIs.

Conceptually:

``` text
Link click
   ↓
history/navigation update
   ↓
URL changes
   ↓
router matches route
   ↓
React renders matching UI
```

The page does not necessarily perform a full document reload.

------------------------------------------------------------------------

# 47. Layout routes

A layout allows common UI around nested routes:

``` text
              Dashboard Layout
           ┌──────────────────────┐
           │ Header               │
           │                      │
           │ Sidebar   Outlet     │
           │           │          │
           │           ▼          │
           │       Current route  │
           │                      │
           └──────────────────────┘
```

Conceptually:

``` jsx
<DashboardLayout>
    <Outlet />
</DashboardLayout>
```

The nested route fills the outlet.

------------------------------------------------------------------------

# 48. Error Boundaries

Rendering errors need a different mechanism from ordinary `try/catch`.

A normal:

``` js
try {
    ...
} catch {}
```

does not automatically catch errors thrown later during React rendering.

Error Boundaries provide a UI fallback for certain rendering errors.

Mental model:

``` text
App
 │
 ├── Header
 ├── Main
 │    │
 │    └── Error
 │
 └── Footer

Boundary
   ↓
fallback UI
```

------------------------------------------------------------------------

# 49. Suspense and lazy loading

Code splitting allows a large application to avoid loading every module
immediately.

Conceptually:

``` text
Initial bundle
   ↓
App
   ↓
user navigates to Admin
   ↓
load Admin chunk
   ↓
Suspense fallback
   ↓
Admin loaded
   ↓
render Admin
```

Typical API:

``` jsx
const Admin = lazy(() => import("./Admin"));

<Suspense fallback={<Loading />}>
    <Admin />
</Suspense>
```

Suspense is also part of the architecture used by modern React features
beyond simple code splitting.

------------------------------------------------------------------------

# 50. Data fetching

A simple React data-fetching flow:

``` text
component
   ↓
request
   ↓
loading state
   ↓
server response
   ↓
success/error state
   ↓
render
```

Typical state:

``` js
{
    data: null,
    loading: true,
    error: null
}
```

But server data has characteristics different from ordinary UI state:

``` text
Server state
────────────
remote
shared
cacheable
stale
refetchable
asynchronous
```

This distinction becomes important for tools such as RTK Query and
TanStack Query.

------------------------------------------------------------------------

# 51. Redux

Redux is independent of React.

It is a JavaScript library for predictable global state management.

Mental model:

``` text
                  Redux Store
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
      user           cart         products
```

Redux's basic cycle:

``` text
component
   │
   ▼
dispatch(action)
   │
   ▼
middleware
   │
   ▼
reducer
   │
   ▼
new state
   │
   ▼
store
   │
   ▼
React-Redux subscription
   │
   ▼
component reads selected state
```

------------------------------------------------------------------------

# 52. Why Context and Redux are different

Context:

``` text
"Make this value available in a subtree."
```

Redux:

``` text
"Maintain centralized application state
with a predictable update architecture."
```

Context does not inherently provide Redux's:

-   action model
-   reducers
-   middleware
-   centralized store
-   Redux DevTools
-   selectors
-   Redux async ecosystem

Context can still be sufficient for many applications.

------------------------------------------------------------------------

# 53. Redux Toolkit

Redux Toolkit is the official recommended way to write modern Redux
logic.

Core APIs:

``` text
configureStore
createSlice
createAsyncThunk
createSelector
RTK Query
```

Typical setup:

``` js
const counterSlice = createSlice({
    name: "counter",

    initialState: {
        value: 0
    },

    reducers: {
        increment(state) {
            state.value += 1;
        }
    }
});
```

Then:

``` js
const store = configureStore({
    reducer: {
        counter: counterSlice.reducer
    }
});
```

------------------------------------------------------------------------

# 54. Why RTK can look like mutation

This:

``` js
state.value += 1;
```

looks like direct mutation.

RTK uses Immer internally so reducers can use mutation-like syntax while
producing immutable state updates.

Mental model:

``` text
Your reducer code
state.value += 1
       │
       ▼
Immer
       │
       ▼
immutable next state
```

This removes much of the boilerplate of older Redux patterns.

------------------------------------------------------------------------

# 55. React-Redux

Redux itself does not know about React.

React-Redux is the official binding layer.

``` text
Redux Store
    │
    ▼
React-Redux
    │
    ├── Provider
    ├── useSelector
    └── useDispatch
    │
    ▼
React components
```

### `Provider`

Makes the Redux store available to the React tree.

### `useSelector`

Reads selected state:

``` js
const cart = useSelector(state => state.cart);
```

### `useDispatch`

Gets the dispatch function:

``` js
const dispatch = useDispatch();

dispatch(addItem(product));
```

Important:

``` text
dispatch(reducer())
```

is not the normal model.

Instead:

``` text
action creator
     ↓
action
     ↓
dispatch(action)
     ↓
Redux
     ↓
reducer
```

------------------------------------------------------------------------

# 56. RTK Query

RTK Query addresses server-data fetching and caching.

Mental model:

``` text
Component
   │
   ▼
query hook
   │
   ▼
cache
   │
   ├── existing data → use it
   │
   └── missing/stale → request
                       │
                       ▼
                    server
                       │
                       ▼
                     cache
                       │
                       ▼
                   component
```

This is different from ordinary UI state such as:

``` text
modalOpen
selectedTab
inputValue
```

------------------------------------------------------------------------

# 57. React state categories

A useful architecture is:

``` text
Application data
│
├── Local UI state
│   ├── modal open
│   ├── selected tab
│   └── input value
│
├── Shared client state
│   ├── theme
│   ├── user preferences
│   └── app-wide state
│
├── Server state
│   ├── products
│   ├── orders
│   └── messages
│
└── URL state
    ├── search params
    ├── route params
    └── current route
```

Different kinds of state do not necessarily need the same tool.

------------------------------------------------------------------------

# 58. Modern React: purity

React rendering should be pure:

``` text
same inputs
    ↓
same output
```

A component should calculate UI rather than perform arbitrary side
effects during render.

Bad conceptual pattern:

``` jsx
function Component() {
    database.write(...); // side effect during render
    return <div />;
}
```

Good separation:

``` text
render
──────
calculate UI

event handler
─────────────
respond to user action

Effect
──────
synchronize with external system caused by rendering
```

------------------------------------------------------------------------

# 59. Strict Mode

Strict Mode is a development-time tool that helps reveal unsafe
patterns.

One thing developers commonly observe is extra development-only
rendering/effect behavior.

Do not interpret that as:

> "Production React always renders twice."

Instead:

``` text
Development + StrictMode
        ↓
extra checks / repeated calls
        ↓
surface impure logic
```

This is useful because React expects rendering logic to be pure.

------------------------------------------------------------------------

# 60. Concurrent React --- conceptual level

Modern React can schedule rendering work rather than treating every
rendering task as equally urgent.

The core idea:

``` text
Urgent
──────
typing
click response
direct interaction

Less urgent
───────────
large UI update
search result rendering
non-critical transition
```

APIs such as:

``` text
startTransition
useTransition
useDeferredValue
```

help express priority relationships.

The key idea is not:

> "React makes JavaScript multithreaded."

It does not.

Rather:

> React can organize and interrupt its own rendering work so
> higher-priority work can be handled appropriately.

------------------------------------------------------------------------

# 61. Server rendering and hydration

A React application can also be rendered on a server.

Conceptually:

``` text
SERVER
  │
  ▼
React renders HTML
  │
  ▼
HTML sent to browser
  │
  ▼
Browser displays HTML
  │
  ▼
React client code loads
  │
  ▼
Hydration
  │
  ▼
HTML becomes interactive React UI
```

Hydration is not the same as initial client rendering.

It means React attaches its behavior to existing server-generated
markup.

------------------------------------------------------------------------

# 62. Server Components

Modern React also supports a server/client component architecture in
environments that implement it.

Conceptually:

``` text
SERVER
├── Server Components
│       │
│       └── can access server-side resources
│
└──────────────┐
               ▼
         rendered output
               │
               ▼
            CLIENT
               │
               └── Client Components
```

The exact server-component architecture is primarily provided through
frameworks/tooling such as Next.js.

Important distinction:

``` text
React
─────
UI library + component model

Next.js
───────
framework providing routing,
server rendering, server components,
data/cache conventions, etc.
```

------------------------------------------------------------------------

# 63. The complete React runtime model

Now combine everything:

``` text
┌─────────────────────────────────────────────────────────────────┐
│                         BROWSER                                 │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    JAVASCRIPT                             │  │
│  │                                                           │  │
│  │  Application                                              │  │
│  │      │                                                    │  │
│  │      ▼                                                    │  │
│  │  Components                                               │  │
│  │      │                                                    │  │
│  │      ▼                                                    │  │
│  │  JSX                                                      │  │
│  │      │                                                    │  │
│  │      ▼                                                    │  │
│  │  React Elements                                           │  │
│  │      │                                                    │  │
│  │      ▼                                                    │  │
│  │  Fiber Tree                                               │  │
│  │      │                                                    │  │
│  │      ├────────── Hooks / State                            │  │
│  │      ├────────── Context                                  │  │
│  │      ├────────── Update queues                            │  │
│  │      └────────── Scheduling                               │  │
│  │      │                                                    │  │
│  │      ▼                                                    │  │
│  │  Render Work                                              │  │
│  │      │                                                    │  │
│  │      ▼                                                    │  │
│  │  Reconciliation                                           │  │
│  │      │                                                    │  │
│  │      ▼                                                    │  │
│  │  Commit                                                   │  │
│  └──────┼────────────────────────────────────────────────────┘  │
│         ▼                                                       │
│  ReactDOM Renderer                                              │
│         │                                                       │
│         ▼                                                       │
│  Browser DOM                                                   │
│         │                                                       │
│         ▼                                                       │
│  Style → Layout → Paint → Composite                            │
│         │                                                       │
│         ▼                                                       │
│       SCREEN                                                    │
└─────────────────────────────────────────────────────────────────┘
```

------------------------------------------------------------------------

# 64. The complete state-update flow

When a user clicks:

``` text
┌──────────────────────┐
│ User clicks button   │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ React event handler  │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ setCount(...)        │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ update queued        │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ scheduling           │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ Fiber work           │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ component executes   │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ new UI representation│
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ reconciliation       │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ commit               │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ ReactDOM updates DOM │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ browser rendering    │
└──────────────────────┘
```

------------------------------------------------------------------------

# 65. The complete component mental model

A component is best thought of as:

``` text
              COMPONENT
                  │
       ┌──────────┼───────────┐
       ▼          ▼           ▼
     props       state       context
       │          │           │
       └──────────┼───────────┘
                  ▼
            render logic
                  │
                  ▼
               JSX
                  │
                  ▼
           React element tree
```

But remember:

> A component function is not a persistent object containing its state.

It is repeatedly executed by React.

React maintains the persistent information around those executions.

------------------------------------------------------------------------

# 66. The React identity model

Identity matters everywhere:

``` text
Component identity
       │
       ├── type
       ├── position
       └── key
```

This affects:

-   state preservation
-   list reconciliation
-   mounting
-   unmounting
-   Effects
-   DOM reuse

A key is therefore not merely a performance trick.

It helps React understand identity across renders.

------------------------------------------------------------------------

# 67. Why React can "render" without changing the DOM

Suppose:

``` jsx
return <h1>Hello</h1>;
```

The component executes again.

React calculates another representation:

``` text
previous: <h1>Hello</h1>
new:      <h1>Hello</h1>
```

No meaningful difference.

Therefore:

``` text
component executed
      ↓
render happened
      ↓
no DOM mutation required
```

This is why:

> Render does not mean "rewrite the DOM."

------------------------------------------------------------------------

# 68. Why React doesn't simply compare against the real DOM

The browser DOM is an imperative mutable structure.

React wants a declarative model where it can reason about:

``` text
previous UI description
        vs
new UI description
```

It can then calculate what the renderer needs to do.

This separation also enables other renderers.

Conceptually:

``` text
                React reconciler
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
         ReactDOM            React Native
             │                   │
             ▼                   ▼
          Browser              Native
            DOM                platform
```

The reconciliation logic and the host renderer are conceptually
separable.

------------------------------------------------------------------------

# 69. React and the browser have different jobs

``` text
┌─────────────────────────────────────────┐
│ React                                   │
│                                         │
│ components                              │
│ state                                   │
│ props                                   │
│ hooks                                   │
│ reconciliation                          │
│ scheduling                              │
│ commit decisions                        │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│ ReactDOM                                │
│                                         │
│ DOM renderer                            │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│ Browser                                 │
│                                         │
│ DOM APIs                                │
│ style calculation                       │
│ layout                                  │
│ paint                                   │
│ compositing                             │
└─────────────────────────────────────────┘
```

This separation is essential for understanding React correctly.

------------------------------------------------------------------------

# 70. A complete mental model for Hooks

Think of Hooks as APIs into React's component-rendering machinery.

``` text
Component execution
       │
       ├── useState
       │      ↓
       │   persistent state
       │
       ├── useEffect
       │      ↓
       │   external synchronization
       │
       ├── useRef
       │      ↓
       │   persistent mutable container
       │
       ├── useContext
       │      ↓
       │   read context
       │
       ├── useReducer
       │      ↓
       │   state transition model
       │
       ├── useMemo
       │      ↓
       │   cached calculation
       │
       └── useCallback
              ↓
           cached function identity
```

Custom Hooks compose these primitives.

------------------------------------------------------------------------

# 71. What each major tool is for

  Tool               Primary problem
  ------------------ ------------------------------------------
  JSX                Describe UI inside JS
  Components         Encapsulate UI/behavior
  Props              Pass inputs down
  `useState`         Component-local state
  Updater function   Calculate from pending state
  `useEffect`        Synchronize with external systems
  `useRef`           Persistent mutable value / DOM reference
  `useContext`       Distribute values through a subtree
  `useReducer`       Complex state transitions
  `useMemo`          Cache calculated value
  `useCallback`      Cache function identity
  Custom Hook        Reuse stateful logic
  `React.memo`       Skip some unnecessary child renders
  Keys               Preserve child identity
  Suspense           Coordinate loading boundaries
  Router             Map URL → UI
  Redux              Centralized predictable app state
  Redux Toolkit      Modern Redux development
  React-Redux        Connect Redux to React
  RTK Query          Server-data fetching/cache

------------------------------------------------------------------------

# 72. Local state vs Context vs Redux

``` text
                    STATE
                      │
       ┌──────────────┼──────────────┐
       ▼              ▼              ▼
   Local state      Context         Redux
       │              │              │
       ▼              ▼              ▼
 component       subtree value    app-wide
 specific        distribution     centralized
```

Choose the smallest mechanism that solves the problem.

Do not put every state variable into Redux.

------------------------------------------------------------------------

# 73. React learning hierarchy

A useful way to remember the whole system:

``` text
LEVEL 1 — UI DESCRIPTION
────────────────────────
JSX
React elements
Components
Props
Children


LEVEL 2 — UI STATE
───────────────────
useState
Snapshots
Batching
Updater functions
useReducer
Context


LEVEL 3 — EXTERNAL SYSTEMS
───────────────────────────
useEffect
Refs
DOM
network
subscriptions
third-party libraries


LEVEL 4 — REUSE
────────────────
Custom Hooks
Composition
Reusable components


LEVEL 5 — RENDERING
───────────────────
Render
Reconciliation
Identity
Keys
Commit


LEVEL 6 — PERFORMANCE
──────────────────────
memo
useMemo
useCallback
Scheduling
Transitions


LEVEL 7 — APPLICATION ARCHITECTURE
───────────────────────────────────
Routing
Forms
Data fetching
Server state
Redux
RTK
RTK Query


LEVEL 8 — INTERNALS
────────────────────
Fiber
Hook storage
Update queues
Scheduling
Lanes/priorities
Work loop
Reconciliation internals
Commit internals
```

------------------------------------------------------------------------

# 74. What "React mastery" actually means

You do not need to memorize every React API.

You should be able to answer:

### State

> Where does state live?

React's internal state associated with component identity/position, not
inside a persistent local variable in the function.

### Snapshot

> Why doesn't `count` change immediately after `setCount()`?

Because the current render has a fixed state snapshot.

### Batching

> Why don't three setters necessarily produce three visible renders?

React batches updates and processes queued updates together.

### Updater

> Why does `prev => prev + 1` work three times?

Each updater receives the pending state produced by the previous update.

### Effects

> Why use `useEffect`?

To synchronize with external systems caused by rendering.

### Ref

> Why use `useRef`?

For persistent mutable values/imperative references that do not
themselves trigger rendering.

### Context

> Why use Context?

To make values available through a component subtree without prop
drilling.

### Redux

> Why use Redux?

For centralized predictable application state and state-update
architecture when local state/context are insufficient.

### Keys

> Why keys?

To communicate child identity across renders.

### Fiber

> Why Fiber?

To represent and schedule React rendering work in units that can be
prioritized, paused, resumed, reused, or abandoned.

------------------------------------------------------------------------

# 75. The final architecture

If you understand the following diagram, you have the core React mental
model:

``` text
┌─────────────────────────────────────────────────────────────────────┐
│                              APP                                   │
│                                                                     │
│  Props ────────┐                                                    │
│  Context ──────┤                                                    │
│  Redux ────────┤                                                    │
│                ▼                                                    │
│         ┌──────────────┐                                            │
│         │  COMPONENT   │                                            │
│         └──────┬───────┘                                            │
│                │                                                    │
│        ┌───────┼────────┬─────────┐                                │
│        ▼       ▼        ▼         ▼                                │
│     useState  Effect   Ref     Context                             │
│        │       │        │         │                                │
│        └───────┴────────┴─────────┘                                │
│                │                                                    │
│                ▼                                                    │
│              JSX                                                    │
│                │                                                    │
│                ▼                                                    │
│         React Elements                                              │
│                │                                                    │
│                ▼                                                    │
│          ┌─────────────┐                                            │
│          │ Fiber Tree  │                                            │
│          └──────┬──────┘                                            │
│                 │                                                    │
│       ┌─────────┼─────────────┐                                     │
│       ▼         ▼             ▼                                     │
│    Hooks     Scheduling   Update queues                             │
│       │         │             │                                     │
│       └─────────┼─────────────┘                                     │
│                 ▼                                                   │
│              RENDER                                                 │
│                 │                                                   │
│                 ▼                                                   │
│          RECONCILIATION                                             │
│                 │                                                   │
│                 ▼                                                   │
│              COMMIT                                                 │
└─────────────────┼───────────────────────────────────────────────────┘
                  ▼
          ┌──────────────────┐
          │     ReactDOM     │
          └────────┬─────────┘
                   ▼
          ┌──────────────────┐
          │   Browser DOM    │
          └────────┬─────────┘
                   ▼
       ┌──────────────────────────┐
       │ Browser rendering        │
       │                          │
       │ Style → Layout → Paint   │
       │ → Composite              │
       └────────────┬─────────────┘
                    ▼
                  SCREEN
```

------------------------------------------------------------------------

# 76. The most important distinctions

Memorize these distinctions, not isolated definitions.

``` text
Component
≠
DOM element
```

``` text
JSX
≠
HTML
```

``` text
React element
≠
DOM element
```

``` text
Render
≠
DOM update
```

``` text
DOM update
≠
browser paint
```

``` text
State snapshot
≠
mutable JavaScript variable
```

``` text
setState
≠
immediate mutation of current snapshot
```

``` text
Updater function
≠
Promise callback
```

``` text
Context
≠
complete global state-management architecture
```

``` text
Redux
≠
React
```

``` text
Redux Toolkit
≠
React-Redux
```

``` text
useMemo
≠
useCallback
```

``` text
useCallback
≠
automatically better performance
```

``` text
Fiber
≠
"the Virtual DOM"
```

``` text
Reconciliation
≠
comparing React directly to the browser DOM
```

------------------------------------------------------------------------

# 77. A practical React application architecture

A mature React application may look like:

``` text
src/
│
├── app/
│   ├── store.js
│   ├── router.jsx
│   └── providers.jsx
│
├── components/
│   ├── Button/
│   ├── Input/
│   ├── Modal/
│   └── Card/
│
├── features/
│   ├── auth/
│   ├── cart/
│   ├── products/
│   └── checkout/
│
├── hooks/
│   ├── useAuth.js
│   ├── useDebounce.js
│   └── useWindowSize.js
│
├── pages/
│   ├── Home/
│   ├── Products/
│   └── Checkout/
│
├── services/
│   └── api.js
│
└── main.jsx
```

The exact structure is a design choice, not a React requirement.

------------------------------------------------------------------------

# 78. Debugging React systematically

When something behaves unexpectedly, ask:

``` text
1. What is the current state snapshot?

2. What triggered the render?

3. What props changed?

4. Did the component identity change?

5. Did a key change?

6. Did context change?

7. Did an Effect synchronize something?

8. Is there a stale closure?

9. Is a ref being used where state is needed?

10. Is the issue actually React, or the browser/DOM/CSS/network?
```

This prevents "try random Hooks until it works" development.

------------------------------------------------------------------------

# 79. The ultimate mental model

React is best understood as a system that lets you describe UI as a
function of application state and then manages the work required to keep
the rendered environment synchronized with that description.

At the application level:

``` text
STATE
  ↓
COMPONENT
  ↓
JSX
  ↓
UI DESCRIPTION
```

At the React level:

``` text
UI DESCRIPTION
  ↓
FIBER / WORK
  ↓
RENDER
  ↓
RECONCILIATION
  ↓
COMMIT
```

At the platform level:

``` text
COMMIT
  ↓
DOM
  ↓
STYLE
  ↓
LAYOUT
  ↓
PAINT
  ↓
SCREEN
```

That is React's central machine.

------------------------------------------------------------------------

# 80. Further internal topics to study after this README

This README gives the complete conceptual/application map. For a true
source-level internals deep dive, continue with:

``` text
React internals
│
├── FiberNode structure
├── FiberRoot
├── current / workInProgress
├── alternate
├── Hook linked list
├── mountWorkInProgressHook
├── updateWorkInProgressHook
├── state update queues
├── dispatchSetState
├── lanes and priorities
├── scheduling
├── work loop
├── beginWork
├── completeWork
├── reconciliation child algorithm
├── keys and identity algorithm
├── effect flags
├── commit phases
├── passive effects
├── host config / ReactDOM renderer
└── concurrent rendering
```

Those details should be learned **after** the API mental model is solid.

------------------------------------------------------------------------

# 81. Official references

React:

-   https://react.dev/learn
-   https://react.dev/learn/state-as-a-snapshot
-   https://react.dev/learn/render-and-commit
-   https://react.dev/learn/synchronizing-with-effects
-   https://react.dev/learn/referencing-values-with-refs
-   https://react.dev/learn/preserving-and-resetting-state
-   https://react.dev/reference/react
-   https://react.dev/reference/rules

React Fiber architecture:

-   https://github.com/acdlite/react-fiber-architecture

Redux:

-   https://redux.js.org/
-   https://redux.js.org/usage/
-   https://redux.js.org/tutorials/quick-start
-   https://redux-toolkit.js.org/

React Router:

-   https://reactrouter.com/

------------------------------------------------------------------------

# 82. Final cheat sheet

``` text
React
│
├── WHY?
│   └── Declarative state → UI synchronization
│
├── UI
│   ├── JSX
│   ├── Elements
│   ├── Components
│   ├── Props
│   └── Composition
│
├── STATE
│   ├── useState
│   ├── snapshots
│   ├── batching
│   ├── updater functions
│   └── useReducer
│
├── SHARING
│   ├── props
│   ├── children
│   ├── Context
│   └── Redux
│
├── SIDE EFFECTS / IMPERATIVE ACCESS
│   ├── useEffect
│   └── useRef
│
├── REUSE
│   └── Custom Hooks
│
├── OPTIMIZATION
│   ├── React.memo
│   ├── useMemo
│   └── useCallback
│
├── IDENTITY
│   ├── component type
│   ├── position
│   └── keys
│
├── APPLICATION
│   ├── Forms
│   ├── Routing
│   ├── Data fetching
│   └── Server state
│
├── REDUX ECOSYSTEM
│   ├── Redux store
│   ├── actions
│   ├── reducers
│   ├── middleware
│   ├── Redux Toolkit
│   ├── React-Redux
│   └── RTK Query
│
└── INTERNALS
    ├── React elements
    ├── Fiber
    ├── Hooks storage
    ├── update queues
    ├── scheduling
    ├── reconciliation
    ├── commit
    └── ReactDOM
```

------------------------------------------------------------------------

## The one sentence

> **React lets you describe UI as a function of state; React maintains
> the persistent information and rendering work needed to turn
> successive UI descriptions into efficient host-environment updates,
> while Hooks, Context, composition, routing, and state-management tools
> provide higher-level ways to structure the application.** ---

# 82. Practical React application basics we have learned

Before moving into the full production-application ecosystem, there are
a few practical patterns that are basic building blocks for a real React
application.

``` text
                         REACT APPLICATION
                                │
        ┌───────────────────────┼────────────────────────┐
        ▼                       ▼                        ▼
   API / Service              Forms                 Security
      layer                    │                        │
        │                  useForm only          Protected routes
      Axios                 validation           Protected features
        │
        └──────────────────────┬─────────────────────────
                               ▼
                         Backend APIs
```

The practical patterns covered here are:

``` text
1. Service layer
       ↓
   separate API communication from components

2. useForm
       ↓
   manage form values, validation, errors and submission

3. useRef + forwardRef
       ↓
   persistent mutable values + imperative access through components

4. Protected routes / components
       ↓
   restrict UI/navigation based on authentication and authorization

5. Frontend data/state architecture
       ↓
   decide where API data lives, when to fetch it, when to reuse it,
   and when it should be fetched again
```

## 82.1 Service layer

A component should not have to know every detail about how an HTTP
request is performed.

Instead of putting Axios calls directly inside a large component,
separate backend operations into a service/API layer:

``` text
Component
    │
    ▼
Service
    │
    ▼
Axios client
    │
    ▼
Backend API
```

Example:

``` js
// services/productService.js

export const productService = {

    getAll() {
        return api.get("/products");
    },

    getById(id) {
        return api.get(`/products/${id}`);
    },

    create(product) {
        return api.post("/products", product);
    },

    update(id, product) {
        return api.put(`/products/${id}`, product);
    },

    remove(id) {
        return api.delete(`/products/${id}`);
    }
};
```

Then the component expresses **what it needs**:

``` js
const response = await productService.getAll();
```

The deeper separation is:

``` text
Component
─────────
UI + user interaction
        │
        ▼
Service
───────
backend operations
        │
        ▼
Axios
─────
HTTP transport
        │
        ▼
Backend
```

A service layer is an **abstraction boundary**, not a different HTTP
technology.

------------------------------------------------------------------------

## 82.2 `useForm` for forms and validation

For the form layer, we can use React Hook Form without introducing
another validation library.

``` jsx
const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
} = useForm();
```

Register an input:

``` jsx
<input
    {...register("email", {
        required: "Email is required",
        pattern: {
            value: /^\S+@\S+$/i,
            message: "Invalid email"
        }
    })}
/>
```

Display the error:

``` jsx
{errors.email && (
    <p>{errors.email.message}</p>
)}
```

Submission:

``` jsx
<form onSubmit={handleSubmit(onSubmit)}>
```

The flow is:

``` text
User enters data
       ↓
React Hook Form
       ↓
field validation
       ↓
 ┌───────────────┐
 ▼               ▼
valid          invalid
 │               │
 ▼               ▼
onSubmit()      errors
 │
 ▼
service
 │
 ▼
Axios
 │
 ▼
Backend
```

Useful `useForm` APIs to know:

``` text
register()
handleSubmit()
formState.errors
formState.isSubmitting
formState.isDirty
watch()
setValue()
getValues()
reset()
```

The important mental model is:

``` text
useForm
   ↓
form management
   ├── values
   ├── validation
   ├── errors
   ├── dirty/touched state
   ├── submission state
   └── reset
```

------------------------------------------------------------------------

## 82.3 `useRef` and `forwardRef`

`useRef` provides a persistent mutable container:

``` jsx
const ref = useRef(null);
```

The key distinction from state is:

``` text
useState
────────
persistent value
+
setter
+
render scheduling


useRef
──────
persistent mutable value
+
mutation does NOT schedule a render
```

Common uses include:

``` text
DOM node
timer ID
WebSocket
previous value
AbortController
third-party instance
imperative integration
```

For DOM access:

``` jsx
const inputRef = useRef(null);

<input ref={inputRef} />

inputRef.current.focus();
```

When a ref needs to cross a custom component boundary, `forwardRef` was
historically the standard mechanism:

``` jsx
const MyInput = forwardRef(function MyInput(props, ref) {
    return <input ref={ref} />;
});
```

Parent:

``` jsx
const inputRef = useRef(null);

<MyInput ref={inputRef} />

inputRef.current.focus();
```

Conceptually:

``` text
Parent
  │
  │ ref
  ▼
MyInput
  │
  │ forwarded ref
  ▼
<input>
  │
  ▼
inputRef.current
```

Modern React also has newer ref-handling capabilities, so `forwardRef`
is especially important to recognize when reading existing code.

------------------------------------------------------------------------

## 82.4 Protected routes and components

Authentication answers:

``` text
"Who is the user?"
```

Authorization answers:

``` text
"What is this user allowed to do?"
```

A protected route can enforce authentication before rendering a page:

``` jsx
function ProtectedRoute({ children }) {
    const { user, loading } = useAuth();

    if (loading) {
        return <Loading />;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return children;
}
```

Usage:

``` jsx
<Route
    path="/profile"
    element={
        <ProtectedRoute>
            <Profile />
        </ProtectedRoute>
    }
/>
```

For role-based access:

``` jsx
<ProtectedRoute allowedRoles={["ADMIN"]}>
    <AdminDashboard />
</ProtectedRoute>
```

For individual features:

``` jsx
<ProtectedFeature permission="product:delete">
    <DeleteButton />
</ProtectedFeature>
```

So:

``` text
AUTHORIZATION
      │
      ├───────────────────────────┐
      ▼                           ▼
Route protection             Feature protection
      │                           │
      ▼                           ▼
Can user enter page?         Can user use feature?
```

Frontend authorization is **not the security boundary**. The backend
must independently authenticate and authorize protected API requests.

------------------------------------------------------------------------

# 82.5 Planning frontend state when backend APIs already exist

This is an important architectural question:

> "The backend APIs are ready. When should the frontend fetch data,
> where should it store that data, when should it reuse it, and when
> should it fetch again?"

Do **not** start by asking:

> "Which data should I put into Redux?"

Start by asking:

> **"What kind of state is this, who needs it, and how long should it
> live?"**

A useful first classification is:

``` text
                         FRONTEND STATE
                              │
          ┌───────────────────┼────────────────────┐
          ▼                   ▼                    ▼
      Local UI            Shared client         Server data
          │                   │                    │
     modalOpen             theme                products
     selectedTab           preferences           orders
     input value            auth UI              users
          │                   │                    │
       useState          Context / Redux      query/cache layer
```

There is also URL state:

``` text
URL state
 ├── /products/42
 ├── ?search=laptop
 ├── ?page=2
 └── ?sort=price
```

Do not automatically put every API response into Redux.

------------------------------------------------------------------------

## 82.6 The most important question: who owns the data?

Suppose `/products` returns:

``` js
[
    { id: 1, name: "Laptop" },
    { id: 2, name: "Phone" }
]
```

Ask:

``` text
Who needs this data?
```

If only `ProductsPage` needs it:

``` text
ProductsPage
    │
    └── products
```

There is no reason to make it global merely because it came from an API.

If several unrelated parts of the application need the same server data:

``` text
                products
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
   ProductList   Search      Cart summary
```

then shared server-state caching is useful.

This is where tools such as RTK Query or TanStack Query fit.

------------------------------------------------------------------------

## 82.7 Don't confuse Redux client state with server state

This distinction is extremely important.

``` text
CLIENT STATE
────────────
"How should my application behave?"

Examples:
    sidebarOpen
    selectedTab
    theme
    local preferences


SERVER STATE
────────────
"What data currently exists on the backend?"

Examples:
    products
    orders
    users
    messages
```

Server data has additional problems:

``` text
remote
asynchronous
shared
cacheable
stale
refetchable
```

That is why simply doing:

``` text
API response
    ↓
Redux slice
```

for every API is often unnecessary.

A server-state tool can manage:

``` text
fetching
caching
staleness
deduplication
refetching
invalidation
loading
errors
```

------------------------------------------------------------------------

## 82.8 When should the frontend load data?

Think in terms of **screen requirements**.

Suppose:

``` text
/products
```

needs the product list.

When the page needs the data:

``` text
Navigate to /products
        ↓
Products page mounts
        ↓
request products
        ↓
loading
        ↓
response
        ↓
render products
```

If the data is already cached and still usable:

``` text
Navigate to /products
        ↓
check cache
        ↓
data available
        ↓
reuse it
```

You don't necessarily want:

``` text
Every component mount
        ↓
ALWAYS call backend
```

because that can create unnecessary requests.

------------------------------------------------------------------------

## 82.9 When should data be loaded globally?

Only load data globally when many parts of the application genuinely
need it or it is part of application initialization.

For example:

``` text
Application startup
        │
        ├── current authenticated user
        ├── permissions
        └── essential application configuration
```

Potentially:

``` text
AuthProvider / Redux
        ↓
currentUser
        ↓
many components consume it
```

But don't do:

``` text
Application startup
        ↓
fetch ALL products
fetch ALL orders
fetch ALL categories
fetch ALL users
fetch ALL notifications
...
```

just because they might eventually be needed.

Prefer loading data close to where it is required.

------------------------------------------------------------------------

## 82.10 When should data be fetched again?

This is fundamentally a **freshness** question.

Ask:

``` text
Has the data potentially become stale?
```

Common reasons to refetch:

``` text
1. User explicitly refreshes
2. Data was invalidated after a mutation
3. Data became stale according to your cache policy
4. User returns to a screen and fresh data is required
5. Window/application becomes active, if appropriate
6. A relevant event says the server data changed
```

Example:

``` text
GET /products
      ↓
cache products
      ↓
User edits product
      ↓
PUT /products/42
      ↓
product changed on server
      ↓
invalidate products
      ↓
fetch/revalidate product list
```

The important idea is:

> **Mutations can make previously fetched server data stale.**

------------------------------------------------------------------------

## 82.11 A practical decision tree

When implementing a screen, ask these questions in order:

``` text
                 I need some data
                       │
                       ▼
             Is it derived only from
             existing frontend state?
                    /       \
                  YES        NO
                   │          │
                   ▼          ▼
               calculate    API data
                              │
                              ▼
                    Is it server state?
                              │
                              ▼
                     use query/cache
                              │
                              ▼
                  Does many UI areas need it?
                       /             \
                     NO              YES
                      │                │
                      ▼                ▼
               keep it local       shared cache
                                    / store
```

For non-server state:

``` text
Does only one component need it?
        │
       YES
        ↓
    useState

Do many components need it?
        │
       YES
        ↓
Context / Redux
```

For server state:

``` text
API data
   ↓
query/cache layer
   ↓
reuse cached data
   ↓
refetch/invalidate when stale
```

------------------------------------------------------------------------

## 82.12 Example: product page architecture

Imagine your backend provides:

``` text
GET    /products
GET    /products/:id
POST   /products
PUT    /products/:id
DELETE /products/:id
```

Frontend:

``` text
                    PRODUCT FEATURE
                          │
          ┌───────────────┼────────────────┐
          ▼               ▼                ▼
      ProductList     ProductDetails    ProductForm
          │               │                │
          ▼               ▼                ▼
       query          query(id)          useForm
          │               │                │
          └───────────────┼────────────────┘
                          ▼
                    productService
                          │
                          ▼
                       Axios
                          │
                          ▼
                     Backend API
```

After:

``` text
POST /products
```

you don't blindly reload every page.

Instead:

``` text
create product
      ↓
mutation succeeds
      ↓
products data is now potentially stale
      ↓
invalidate/revalidate product list
      ↓
ProductList gets fresh data
```

This gives you a much cleaner architecture.

------------------------------------------------------------------------

## 82.13 The frontend architecture to aim for

When the backend is already designed, your frontend can be planned as:

``` text
                        BACKEND API
                             ▲
                             │
                           Axios
                             ▲
                             │
                      Service / API layer
                             ▲
                             │
                 Query / mutation layer
                             ▲
                             │
              ┌──────────────┴──────────────┐
              │                             │
          Page components              Feature components
              │                             │
       ┌──────┼─────────┐             ┌─────┼─────┐
       ▼      ▼         ▼             ▼           ▼
     UI     useForm   local state   UI        protected feature
```

And for state:

``` text
LOCAL UI STATE
    ↓
useState / useReducer

SHARED CLIENT STATE
    ↓
Context / Redux

SERVER STATE
    ↓
RTK Query / TanStack Query / equivalent cache

URL STATE
    ↓
Router / search params

FORM STATE
    ↓
React Hook Form
```

The biggest architectural rule is:

> **Don't put data into Redux simply because it came from an API. Decide
> whether it is local UI state, shared client state, server state, URL
> state, or form state first.**

That decision determines the correct tool.

# PART II --- BUILDING A COMPLETE PRODUCTION REACT APPLICATION

The first part explains React itself.

This second part answers a different question:

> **What else do I actually need to build, test, secure, maintain,
> deploy, and operate a real production React application?**

React alone is intentionally small.

A production application usually needs an ecosystem around it:

``` text
                         PRODUCTION REACT APP
                                  │
      ┌───────────────────────────┼───────────────────────────┐
      ▼                           ▼                           ▼
   UI LAYER                  DATA LAYER                  PLATFORM
      │                           │                           │
      ├── Components              ├── HTTP client             ├── Vite
      ├── Forms                   ├── Server-state cache      ├── Env config
      ├── Styling                 ├── API services           ├── Build
      ├── Accessibility           └── Error handling          └── Deployment
      │
      ├── Validation
      ├── Routing
      └── UX states
                                  │
      ┌───────────────────────────┼───────────────────────────┐
      ▼                           ▼                           ▼
    QUALITY                    SECURITY                    OPERATIONS
      │                           │                           │
      ├── Unit tests              ├── Auth                    ├── Logging
      ├── Integration tests       ├── Authorization            ├── Monitoring
      ├── E2E tests               ├── XSS                       ├── Analytics
      └── Mocking                 ├── CSRF                      └── CI/CD
                                  └── Secrets
```

The important principle is:

> **Do not add libraries because they are popular. Add a layer because
> the application has a problem that layer solves.**

------------------------------------------------------------------------

# 83. Production stack --- the mental map

A practical React stack can look like:

``` text
┌───────────────────────────────────────────────────────────────┐
│                         USER / BROWSER                        │
└───────────────────────────────┬───────────────────────────────┘
                                │
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                         REACT UI                              │
│                                                               │
│ Components • Hooks • Context • Composition                     │
└───────────────────────────────┬───────────────────────────────┘
                                │
                ┌───────────────┼────────────────┐
                ▼               ▼                ▼
             Router           Forms          State
                │               │                │
                │               ├── RHF          ├── local
                │               └── Zod           ├── Context
                │                                └── Redux
                ▼
┌───────────────────────────────────────────────────────────────┐
│                    SERVER DATA LAYER                          │
│                                                               │
│ TanStack Query / RTK Query / custom data layer                │
└───────────────────────────────┬───────────────────────────────┘
                                │
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                         HTTP LAYER                            │
│                                                               │
│ fetch / Axios                                                 │
│                                                               │
│ base URL • headers • auth • errors • cancellation             │
└───────────────────────────────┬───────────────────────────────┘
                                │
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                         BACKEND API                           │
└───────────────────────────────────────────────────────────────┘
```

Around this:

``` text
Testing
Security
Accessibility
Performance
Observability
CI/CD
Deployment
```

------------------------------------------------------------------------

# 84. `fetch` vs Axios

The browser already provides:

``` js
fetch("/api/products");
```

So Axios is not required by React.

Axios is an HTTP client that adds a more structured API around HTTP
requests.

Axios provides concepts such as:

-   instances
-   defaults
-   interceptors
-   request/response configuration
-   response objects
-   cancellation
-   error handling
-   transformations

Official Axios documentation describes it as a promise-based HTTP client
for browsers and Node.js, with interceptors and request cancellation
support.

### Important principle

``` text
React
  ≠
Axios
```

Axios belongs in the application data/network layer.

------------------------------------------------------------------------

# 85. Axios architecture

Do not scatter:

``` js
axios.get(...)
axios.post(...)
axios.get(...)
```

through every component.

Instead:

``` text
Component
    │
    ▼
Hook / Query
    │
    ▼
API function
    │
    ▼
Axios instance
    │
    ▼
Backend
```

Example:

``` text
src/
├── api/
│   ├── client.ts
│   ├── auth.api.ts
│   ├── products.api.ts
│   └── orders.api.ts
│
├── features/
│   └── products/
│       ├── hooks/
│       └── components/
```

This keeps networking out of presentation components.

------------------------------------------------------------------------

# 86. Axios instance

Instead of configuring Axios repeatedly:

``` js
axios.get("https://api.example.com/products");
axios.get("https://api.example.com/users");
```

create one configured instance:

``` js
import axios from "axios";

export const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 10000
});
```

Mental model:

``` text
                    api
                     │
             ┌───────┴────────┐
             ▼                ▼
        GET /products      POST /orders
             │                │
             └───────┬────────┘
                     ▼
                  Axios
                     │
                     ▼
                   API
```

Axios supports per-instance defaults such as `baseURL`, timeout, and
headers.

------------------------------------------------------------------------

# 87. Axios request interceptors

An interceptor sits between your application and the request.

``` text
Application
     │
     ▼
api.get(...)
     │
     ▼
REQUEST INTERCEPTOR
     │
     ├── attach headers
     ├── attach auth information
     └── logging/metadata
     │
     ▼
HTTP request
```

Example:

``` js
api.interceptors.request.use(config => {
    const token = getAccessToken();

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});
```

Be careful with authentication architecture; storing tokens in
browser-accessible storage has security tradeoffs.

------------------------------------------------------------------------

# 88. Axios response interceptors

Response flow:

``` text
Backend
   │
   ▼
Axios response
   │
   ▼
RESPONSE INTERCEPTOR
   │
   ├── normalize errors
   ├── handle common status codes
   ├── logging
   └── optional auth-refresh flow
   │
   ▼
Application
```

Example:

``` js
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            // application-specific auth handling
        }

        return Promise.reject(error);
    }
);
```

Do not put enormous business logic into interceptors.

They should handle cross-cutting HTTP concerns.

------------------------------------------------------------------------

# 89. Token refresh architecture

A production authentication flow may involve:

``` text
Access token
    │
    ├── short-lived
    │
    ▼
API request
    │
    ▼
401
    │
    ▼
refresh authentication
    │
    ▼
new access token
    │
    ▼
retry original request
```

The difficult part is concurrent expiry.

Imagine:

``` text
Request A ──┐
Request B ──┼── all receive 401
Request C ──┘
             │
             ▼
       refresh token
```

You generally do not want three simultaneous refresh requests.

A production implementation may coordinate refresh attempts with a
shared promise/queue.

Also define:

``` text
What happens if refresh fails?
        ↓
clear auth state
        ↓
redirect/show login
```

------------------------------------------------------------------------

# 90. Cancellation

Network requests can outlive the UI that created them.

Example:

``` text
User searches:
"r"
"re"
"rea"
"react"
```

Older requests may finish after newer requests.

You need to prevent stale results from incorrectly winning.

Modern Axios supports `AbortController`:

``` js
const controller = new AbortController();

api.get("/search", {
    signal: controller.signal
});

controller.abort();
```

Mental model:

``` text
Request A ───────────────X cancelled
Request B ───────────────────────→ result
```

This is especially useful for:

-   search
-   autocomplete
-   route changes
-   component unmounting
-   replacing requests

------------------------------------------------------------------------

# 91. Request states

Every production API interaction should conceptually account for:

``` text
idle
 │
 ▼
loading
 ├───────────────┐
 ▼               ▼
success         error
 │               │
 ▼               ▼
data           message
```

And often:

``` text
loading
+
previous data
```

This is why server-state libraries become valuable.

------------------------------------------------------------------------

# 92. Forms are their own subsystem

A serious form is more than:

``` jsx
<input value={name} onChange={...} />
```

Real forms have:

``` text
Form
│
├── field values
├── touched state
├── dirty state
├── validation
├── submission
├── server errors
├── async validation
├── loading state
├── disabled state
├── reset
├── default values
├── dynamic fields
├── accessibility
└── error presentation
```

For large forms, a dedicated form library can reduce boilerplate.

------------------------------------------------------------------------

# 93. React Hook Form

React Hook Form provides:

``` text
useForm
register
handleSubmit
formState
watch
setValue
getValues
reset
setError
clearErrors
useFieldArray
Controller
```

Its design emphasizes performant form handling and reducing unnecessary
re-renders.

Basic mental model:

``` text
                useForm()
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
      fields     errors     submit
        │          │          │
        └──────────┼──────────┘
                   ▼
                 form
```

Example:

``` jsx
const {
    register,
    handleSubmit,
    formState: { errors }
} = useForm();

function onSubmit(data) {
    console.log(data);
}

<form onSubmit={handleSubmit(onSubmit)}>
    <input {...register("email")} />

    {errors.email && (
        <p>{errors.email.message}</p>
    )}

    <button type="submit">
        Submit
    </button>
</form>
```

------------------------------------------------------------------------

# 94. Why form libraries can improve performance

A naive controlled form can do:

``` text
keystroke
   ↓
setState
   ↓
entire component renders
```

A form library can isolate subscriptions:

``` text
field A changes
   ↓
field A subscribers
   ↓
only relevant UI updates
```

This matters especially for:

-   large forms
-   dynamic forms
-   field arrays
-   complex validation
-   nested components

React Hook Form explicitly focuses on minimizing unnecessary re-renders.

------------------------------------------------------------------------

# 95. `register`

React Hook Form can connect an input:

``` jsx
<input {...register("email")} />
```

Conceptually:

``` text
input
 │
 ├── value
 ├── change events
 ├── blur events
 └── validation
       │
       ▼
React Hook Form
```

This removes much of the repetitive wiring normally required for forms.

------------------------------------------------------------------------

# 96. `Controller`

Some UI libraries use controlled components:

``` text
React Select
MUI
Ant Design
custom date picker
rich editor
```

They may not work naturally with `register`.

`Controller` acts as an adapter:

``` text
React Hook Form
       │
       ▼
   Controller
       │
       ▼
controlled component
```

This is a useful production pattern.

------------------------------------------------------------------------

# 97. Dynamic forms

Suppose an order has multiple items:

``` text
Order
├── Item 1
├── Item 2
├── Item 3
└── Add item
```

A form library's field-array abstraction can model:

``` js
[
    { productId: 1, quantity: 2 },
    { productId: 4, quantity: 1 }
]
```

This is significantly cleaner than manually synchronizing many
independent state variables.

------------------------------------------------------------------------

# 98. Form validation

Validation has multiple layers.

``` text
                    INPUT
                      │
                      ▼
             ┌─────────────────┐
             │ Client validation│
             └────────┬────────┘
                      │
                 valid?
                 /       \
               no         yes
               │           │
               ▼           ▼
           show error    submit
                           │
                           ▼
                    SERVER VALIDATION
                           │
                       valid?
                      /      \
                    no        yes
                    │          │
                    ▼          ▼
              server error   success
```

Never rely exclusively on client validation.

The browser is controlled by the user.

------------------------------------------------------------------------

# 99. Zod

Zod is a schema validation library with TypeScript-first type inference.

Example:

``` ts
import * as z from "zod";

const loginSchema = z.object({
    email: z.email(),
    password: z.string().min(8)
});
```

Now the same schema describes expected data and validates runtime input.

``` text
Schema
  │
  ├── runtime validation
  │
  └── TypeScript inference
```

This is powerful because TypeScript types disappear at runtime, while
Zod schemas still execute.

------------------------------------------------------------------------

# 100. TypeScript vs runtime validation

This distinction is critical.

TypeScript:

``` ts
type User = {
    name: string;
};
```

protects you during development/build time.

But the backend can still send:

``` json
{
    "name": 123
}
```

JavaScript receives runtime data.

Therefore:

``` text
TypeScript
   ↓
compile-time safety


Zod
   ↓
runtime safety
```

A production API boundary often needs both.

------------------------------------------------------------------------

# 101. Zod + React Hook Form

The common architecture:

``` text
                 Form input
                     │
                     ▼
              React Hook Form
                     │
                     ▼
                  Zod
                     │
              ┌──────┴──────┐
              ▼             ▼
            valid          invalid
              │             │
              ▼             ▼
             API        field errors
```

Typical integration uses a resolver:

``` js
useForm({
    resolver: zodResolver(loginSchema)
});
```

Then:

``` text
one schema
    │
    ├── form validation
    ├── API payload validation
    └── inferred TypeScript type
```

This reduces duplicated validation rules.

------------------------------------------------------------------------

# 102. Do not confuse validation with sanitization

Validation asks:

> Is this input acceptable?

Sanitization/transformation asks:

> How should this input be normalized?

Examples:

``` text
"  Sagar  "
      ↓
"Sagar"
```

or:

``` text
"42"
      ↓
42
```

Zod can perform transformations/coercion, but security-sensitive
sanitization must still be designed according to the actual data and
backend context.

------------------------------------------------------------------------

# 103. API response validation

Do not validate only forms.

External APIs are also untrusted boundaries.

``` text
Backend response
       │
       ▼
Zod schema
       │
    valid?
    /   \
  yes    no
  │       │
  ▼       ▼
app      fail safely
```

Example:

``` ts
const ProductSchema = z.object({
    id: z.number(),
    name: z.string(),
    price: z.number()
});

const product = ProductSchema.parse(response.data);
```

This is particularly useful when:

-   backend changes
-   third-party APIs are used
-   multiple teams own frontend/backend
-   generated types are incomplete
-   data is critical

------------------------------------------------------------------------

# 104. API contract thinking

A production application should have a clear contract:

``` text
Frontend
   │
   │ request schema
   ▼
Backend
   │
   │ response schema
   ▼
Frontend
```

Ideally the contract is represented through:

``` text
OpenAPI
GraphQL schema
generated clients
shared schema
runtime validation
```

Avoid manually duplicating dozens of undocumented request/response
shapes.

------------------------------------------------------------------------

# 105. Type-safe API layer

A good pattern:

``` text
components
    │
    ▼
feature hook
    │
    ▼
API function
    │
    ▼
HTTP client
    │
    ▼
schema validation
    │
    ▼
backend
```

Example:

``` ts
export async function getProduct(id: number) {
    const response = await api.get(`/products/${id}`);

    return ProductSchema.parse(response.data);
}
```

Now the UI receives validated data rather than arbitrary `unknown` JSON.

------------------------------------------------------------------------

# 106. TanStack Query

TanStack Query is a dedicated server-state library.

It manages concepts such as:

``` text
queries
mutations
cache
stale data
refetching
retrying
invalidation
loading
error
optimistic updates
```

Architecture:

``` text
                   React
                     │
                     ▼
               useQuery(...)
                     │
                     ▼
             Query Cache
             │    │    │
             │    │    └── stale time
             │    └─────── retries
             └──────────── data
                     │
                     ▼
                  API
```

The current TanStack Query React documentation is for v5.

------------------------------------------------------------------------

# 107. Query keys

A query needs identity.

Example:

``` js
["products", { category: "books", page: 2 }]
```

Mental model:

``` text
query key
    ↓
cache identity
    ↓
cached result
```

If the key changes:

``` text
["products", page 1]
          ↓
["products", page 2]
```

the cache lookup changes.

------------------------------------------------------------------------

# 108. Query vs mutation

A query generally reads server state:

``` text
GET
```

A mutation changes server state:

``` text
POST
PUT
PATCH
DELETE
```

Conceptually:

``` text
QUERY
  ↓
read/cache


MUTATION
  ↓
change server
  ↓
invalidate/update related queries
```

------------------------------------------------------------------------

# 109. Cache invalidation

Suppose:

``` text
GET /products
```

is cached.

Then:

``` text
POST /products
```

creates a new product.

The old cached product list is now potentially stale.

Production architecture:

``` text
Mutation
   │
   ▼
server changes
   │
   ▼
invalidate products query
   │
   ▼
refetch / update cache
   │
   ▼
UI reflects server state
```

This is one of the major reasons server-state libraries exist.

------------------------------------------------------------------------

# 110. Optimistic updates

Sometimes waiting for the server feels slow.

Example:

``` text
User clicks Like
     │
     ▼
UI immediately shows liked
     │
     ▼
send API request
     │
   ┌─┴─────────────┐
   ▼               ▼
success           failure
   │               │
keep change       rollback
```

Optimistic UI requires a rollback strategy.

Never implement:

``` text
"show success immediately and hope the server agrees"
```

without handling failure.

------------------------------------------------------------------------

# 111. Loading states are UX states

A production app should distinguish:

``` text
Initial loading
Refreshing
Submitting
Deleting
Uploading
Retrying
Background fetching
Empty
Error
Success
```

For example:

``` text
Initial page:
    Loading skeleton

Existing data + refresh:
    Keep data
    Show small refresh indicator

Mutation:
    Disable affected action
    Show progress

Error:
    Keep recoverable context
    Show retry
```

This produces much better UX than one global `loading` boolean.

------------------------------------------------------------------------

# 112. Error handling architecture

Do not handle every error with:

``` js
alert(error.message);
```

Create categories:

``` text
ERROR
 │
 ├── Network error
 │
 ├── 400 validation
 │
 ├── 401 unauthenticated
 │
 ├── 403 forbidden
 │
 ├── 404 not found
 │
 ├── 409 conflict
 │
 ├── 422 domain validation
 │
 ├── 429 rate limited
 │
 └── 500 server error
```

The UI can then respond appropriately.

------------------------------------------------------------------------

# 113. Error normalization

Different backends may return different shapes.

Instead of allowing every component to know backend details:

``` text
Backend errors
    │
    ▼
API client
    │
    ▼
normalize
    │
    ▼
ApplicationError
    │
    ▼
UI
```

Example conceptual type:

``` ts
type AppError = {
    kind:
        | "network"
        | "validation"
        | "unauthorized"
        | "forbidden"
        | "notFound"
        | "conflict"
        | "server";

    message: string;

    fieldErrors?: Record<string, string>;
};
```

Now the UI deals with a stable abstraction.

------------------------------------------------------------------------

# 114. Authentication

Authentication answers:

> Who are you?

Authorization answers:

> What are you allowed to do?

Do not confuse them.

``` text
LOGIN
  ↓
authentication
  ↓
identity
  ↓
authorization
  ↓
permissions
```

Example:

``` text
User
├── authenticated
├── role = ADMIN
└── permissions
    ├── products.read
    ├── products.write
    └── users.manage
```

------------------------------------------------------------------------

# 115. Frontend auth architecture

The frontend may need:

``` text
Auth state
│
├── current user
├── authentication status
├── permissions
└── loading
```

A typical flow:

``` text
Application starts
       ↓
restore/check session
       ↓
loading
       ↓
authenticated?
   /         \
 yes          no
 │             │
 ▼             ▼
App          Login
```

The frontend should not be the ultimate security boundary.

The backend must enforce authorization.

------------------------------------------------------------------------

# 116. Protected routes

A route guard can improve UX:

``` text
/private
    │
    ▼
authenticated?
   /       \
 yes        no
 │           │
 ▼           ▼
page       login
```

But this does **not** secure the backend.

A malicious client can call the API directly.

Therefore:

``` text
Frontend route guard
      =
UX


Backend authorization
      =
security
```

------------------------------------------------------------------------

# 117. Token storage

Common choices have tradeoffs.

``` text
localStorage
    │
    └── JavaScript can access it
         → XSS consequences matter

HttpOnly cookie
    │
    └── JavaScript cannot read it
         → CSRF and cookie policy must be handled
```

There is no universal one-line answer.

Authentication architecture depends on:

-   backend
-   same-site/cross-site setup
-   CSRF strategy
-   refresh mechanism
-   threat model

Never blindly copy token storage from a tutorial.

------------------------------------------------------------------------

# 118. CORS

CORS is enforced by browsers.

Example:

``` text
Frontend
http://localhost:5173

        │
        │ request
        ▼

Backend
http://localhost:8080
```

Different origins may require CORS configuration.

CORS is not authentication.

``` text
CORS
────
browser security policy about cross-origin requests


Authentication
──────────────
who is the caller?
```

A server should still authenticate and authorize requests.

------------------------------------------------------------------------

# 119. CSRF

CSRF matters especially when authentication credentials are
automatically attached to requests, such as cookies.

Conceptually:

``` text
Malicious site
      │
      ▼
browser sends credentialed request
      │
      ▼
your backend
```

Mitigations can include:

-   SameSite cookies
-   CSRF tokens
-   origin checks
-   correct CORS policy
-   appropriate cookie configuration

The exact strategy depends on the authentication architecture.

------------------------------------------------------------------------

# 120. XSS

Cross-site scripting occurs when attacker-controlled content becomes
executable script.

Avoid:

``` jsx
<div dangerouslySetInnerHTML={{ __html: userInput }} />
```

unless the HTML is intentionally sanitized and trusted.

Normal React text rendering escapes text content.

Security mental model:

``` text
untrusted input
      ↓
treat as data
      ↓
escape/sanitize according to context
      ↓
render
```

Never assume:

> "React automatically makes every string safe in every context."

URLs, HTML, CSS, third-party widgets, and DOM APIs can have different
security considerations.

------------------------------------------------------------------------

# 121. Environment variables

Frontend environment variables are not secrets.

If a value is shipped to the browser:

``` text
build
  ↓
JavaScript bundle
  ↓
browser
```

the user can inspect it.

Therefore:

``` text
PUBLIC API URL
    ✓ okay


DATABASE PASSWORD
    ✗ never


PRIVATE API KEY
    ✗ never
```

Secrets belong on trusted server infrastructure.

------------------------------------------------------------------------

# 122. Vite environment variables

With Vite, client-exposed variables commonly use:

``` text
VITE_*
```

Example:

``` env
VITE_API_URL=https://api.example.com
```

Then:

``` js
import.meta.env.VITE_API_URL
```

Treat these as public configuration.

------------------------------------------------------------------------

# 123. Configuration by environment

A production app commonly has:

``` text
development
staging
production
```

Example:

``` text
.env
.env.local
.env.development
.env.production
```

The exact files and precedence depend on the build system.

Keep environment-specific configuration separate from source code.

------------------------------------------------------------------------

# 124. Never commit secrets

Do not commit:

``` text
.env
private keys
cloud credentials
database passwords
JWT signing secrets
service account files
```

Use:

``` text
.env.example
```

for documentation:

``` env
VITE_API_URL=
```

and store real values in the deployment platform's secret/configuration
system.

------------------------------------------------------------------------

# 125. Styling architecture

Production React applications need a styling strategy.

Common approaches:

``` text
CSS Modules
Tailwind CSS
plain CSS
CSS-in-JS
component libraries
design systems
```

The important thing is consistency.

------------------------------------------------------------------------

# 126. Tailwind mental model

Tailwind does not replace React.

React:

``` text
state
  ↓
className
```

Tailwind:

``` text
className
  ↓
CSS rules
```

Browser:

``` text
CSS rules
  ↓
style calculation
  ↓
layout
  ↓
paint
```

This separation is important.

------------------------------------------------------------------------

# 127. Design system

A growing application benefits from reusable primitives:

``` text
Button
Input
Select
Modal
Dialog
Card
Table
Badge
Toast
Dropdown
Tooltip
Skeleton
```

Then higher-level features compose them:

``` text
Design system
     ↓
Feature components
     ↓
Pages
     ↓
Application
```

------------------------------------------------------------------------

# 128. Accessibility

A production application must work for keyboard and assistive technology
users.

Important principles:

``` text
semantic HTML
keyboard navigation
focus management
labels
ARIA only when necessary
color contrast
screen reader behavior
error announcements
```

Prefer:

``` html
<button>Save</button>
```

over:

``` html
<div onclick="save()">Save</div>
```

Semantic HTML provides browser behavior for free.

------------------------------------------------------------------------

# 129. Form accessibility

Bad:

``` jsx
<input />
<p>Email invalid</p>
```

Better:

``` jsx
<label htmlFor="email">
    Email
</label>

<input
    id="email"
    aria-invalid={!!errors.email}
    aria-describedby="email-error"
/>

<p id="email-error">
    {errors.email?.message}
</p>
```

The error state should be understandable without relying only on color.

------------------------------------------------------------------------

# 130. Focus management

When opening a modal:

``` text
open modal
   ↓
focus moves into modal
   ↓
keyboard stays within modal
   ↓
close
   ↓
focus returns to trigger
```

This is an example of where refs and imperative DOM APIs can be
appropriate.

------------------------------------------------------------------------

# 131. File uploads

File upload introduces a separate flow:

``` text
<input type="file">
        │
        ▼
File object
        │
        ▼
FormData
        │
        ▼
multipart/form-data
        │
        ▼
backend / object storage
```

Example:

``` js
const formData = new FormData();

formData.append("image", file);

await api.post("/upload", formData);
```

Production considerations:

``` text
file type
file size
upload progress
cancellation
preview
server validation
virus scanning
object storage
signed URLs
```

Never trust client-side file type/size validation as the only security
boundary.

------------------------------------------------------------------------

# 132. Pagination

Large datasets should not always be loaded at once.

Common models:

``` text
Offset pagination

?page=3&limit=20


Cursor pagination

?cursor=abc123&limit=20
```

UI model:

``` text
query parameters
      ↓
API request
      ↓
page data
      ↓
cache
      ↓
render
```

Cursor pagination is often preferable for datasets where records change
frequently, but the right choice depends on the backend.

------------------------------------------------------------------------

# 133. Infinite scrolling

Infinite query architecture:

``` text
page 1
  ↓
user reaches bottom
  ↓
fetch page 2
  ↓
append
  ↓
user reaches bottom
  ↓
fetch page 3
```

Production concerns:

-   loading indicators
-   duplicate requests
-   end-of-list
-   cancellation
-   cache size
-   scroll restoration
-   accessibility

------------------------------------------------------------------------

# 134. Search and debouncing

Without debounce:

``` text
r → request
re → request
rea → request
reac → request
react → request
```

With debounce:

``` text
r
re
rea
reac
react
     │
     │ user pauses
     ▼
one request
```

A custom Hook can model this:

``` js
function useDebounce(value, delay) {
    const [debounced, setDebounced] = useState(value);

    useEffect(() => {
        const id = setTimeout(
            () => setDebounced(value),
            delay
        );

        return () => clearTimeout(id);
    }, [value, delay]);

    return debounced;
}
```

Then:

``` text
input state
   ↓
debounced state
   ↓
query
```

------------------------------------------------------------------------

# 135. Race conditions in search

Debounce alone does not solve all races.

Consider:

``` text
Request A = "rea"
Request B = "react"

B finishes first
A finishes later
```

If A blindly updates state:

``` text
UI shows stale "rea" results
```

Solutions include:

``` text
AbortController
query-library request cancellation
request IDs
query cache identity
server-side request semantics
```

------------------------------------------------------------------------

# 136. WebSockets / real-time data

For real-time apps:

``` text
HTTP
────
request → response


WebSocket
─────────
connection
    ↕
continuous messages
```

React integration:

``` text
component
   ↓
Effect
   ↓
WebSocket connection
   ↓
message
   ↓
state/store/cache update
   ↓
React render
```

Cleanup is critical:

``` js
useEffect(() => {
    const socket = new WebSocket(url);

    socket.onmessage = handleMessage;

    return () => {
        socket.close();
    };
}, [url]);
```

------------------------------------------------------------------------

# 137. Avoiding duplicate subscriptions

This is a common production bug:

``` text
render
 ↓
new subscription

render
 ↓
another subscription

render
 ↓
another subscription
```

The correct pattern is:

``` text
Effect setup
     ↓
subscription
     ↓
cleanup
     ↓
new setup if dependency changes
```

Strict Mode development behavior can expose missing cleanup.

------------------------------------------------------------------------

# 138. Notifications and toasts

A production application often needs transient UI state:

``` text
Saved successfully
Payment failed
Copied
Network unavailable
```

A toast system is usually application-level UI state.

Architecture:

``` text
feature
  ↓
notification action/API
  ↓
toast manager
  ↓
ToastContainer
  ↓
screen
```

Do not create dozens of independent toast libraries or notification
systems.

------------------------------------------------------------------------

# 139. Modals and portals

A modal visually appears above the application.

React can render it through a portal:

``` text
React tree
   │
   ├── App
   │
   └── Modal component
          │
          ▼
       Portal
          │
          ▼
document.body
```

The DOM placement can differ from React tree placement.

This is important for:

-   z-index
-   overflow clipping
-   overlays
-   dialogs

But event/context semantics remain tied to the React tree.

------------------------------------------------------------------------

# 140. Global providers

A mature application often has a provider composition:

``` jsx
<StrictMode>
    <QueryClientProvider client={queryClient}>
        <ReduxProvider store={store}>
            <AuthProvider>
                <RouterProvider router={router} />
            </AuthProvider>
        </ReduxProvider>
    </QueryClientProvider>
</StrictMode>
```

Do not add providers blindly.

Each provider introduces another application-wide dependency.

A useful principle:

``` text
Provider
   ↓
"What cross-tree dependency does this provide?"
```

------------------------------------------------------------------------

# 141. Application initialization

At startup you may need:

``` text
Browser loads bundle
       ↓
initialize app
       ↓
load configuration
       ↓
restore authentication
       ↓
initialize stores/query client
       ↓
initialize router
       ↓
render app
```

Avoid putting every initialization operation inside one gigantic
`App.jsx`.

Create explicit application initialization boundaries.

------------------------------------------------------------------------

# 142. Folder architecture

A scalable feature-oriented structure:

``` text
src/
│
├── app/
│   ├── App.tsx
│   ├── providers/
│   ├── router/
│   └── store/
│
├── components/
│   ├── ui/
│   └── layout/
│
├── features/
│   ├── auth/
│   │   ├── api/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── schemas/
│   │   └── types/
│   │
│   ├── products/
│   │   ├── api/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── schemas/
│   │   └── types/
│   │
│   └── checkout/
│
├── lib/
│   ├── axios.ts
│   ├── query.ts
│   └── logger.ts
│
├── routes/
│
├── hooks/
│
├── utils/
│
├── types/
│
└── main.tsx
```

Feature folders keep related code close together.

------------------------------------------------------------------------

# 143. Feature architecture

Instead of:

``` text
components/
services/
hooks/
utils/
```

with hundreds of unrelated files, a feature can own its pieces:

``` text
features/products/
│
├── api/
│   └── products.api.ts
│
├── components/
│   ├── ProductCard.tsx
│   └── ProductList.tsx
│
├── hooks/
│   └── useProducts.ts
│
├── schemas/
│   └── product.schema.ts
│
└── types/
    └── product.ts
```

The mental model:

``` text
PRODUCT FEATURE
      │
 ┌────┼────┬────┐
 ▼    ▼    ▼    ▼
API  UI  Hooks Schema
```

------------------------------------------------------------------------

# 144. Separation of concerns

A production component should not become:

``` text
UI
+
Axios
+
validation
+
Redux
+
routing
+
business rules
+
data transformation
+
analytics
+
file upload
+
error handling
+
500 lines of JSX
```

Instead:

``` text
Page
  ↓
Feature component
  ↓
Feature hook
  ↓
Data/API layer
  ↓
Backend
```

Each layer has a reason to exist.

------------------------------------------------------------------------

# 145. Service layer

A service/API function should represent backend operations:

``` ts
export const productsApi = {
    getProducts,
    getProduct,
    createProduct,
    updateProduct,
    deleteProduct
};
```

Then:

``` text
Component
    ↓
useProducts()
    ↓
productsApi.getProducts()
    ↓
HTTP client
```

The component does not need to know:

``` text
URL
headers
Axios
serialization
error normalization
```

------------------------------------------------------------------------

# 146. Domain transformation

Backend response:

``` json
{
    "base_price": 100,
    "discount_percentage": 10
}
```

Frontend model:

``` ts
{
    basePrice: 100,
    discountPercentage: 10
}
```

A transformation layer can convert external API models into application
models.

``` text
API DTO
  ↓
mapper
  ↓
domain/application model
  ↓
UI
```

This protects UI code from backend representation details.

------------------------------------------------------------------------

# 147. API DTO vs UI model

Do not automatically use:

``` text
backend JSON = component props
```

They are different concerns.

``` text
Backend DTO
───────────
what server sends


Application model
─────────────────
what application needs


View model
──────────
what a component needs
```

For small apps these may be identical.

For large systems, separation becomes valuable.

------------------------------------------------------------------------

# 148. Optimistic UI and rollback

A production mutation can be:

``` text
Current cache
     │
     ▼
save previous state
     │
     ▼
optimistically update
     │
     ▼
send request
   /      \
success   failure
  │          │
  ▼          ▼
keep       rollback
           + show error
```

Without rollback, optimistic updates can leave the UI lying about server
state.

------------------------------------------------------------------------

# 149. Retry strategy

Not every error should be retried.

Usually:

``` text
network failure
      ✓ potentially retry


500
      ✓ potentially retry


429
      ✓ retry according to server guidance


400
      ✗ usually don't retry


401
      ✗ authenticate/refresh instead
```

Retries need:

``` text
maximum attempts
backoff
jitter
idempotency considerations
```

Do not create infinite retries.

------------------------------------------------------------------------

# 150. Idempotency

Suppose:

``` text
POST /payment
```

network fails after the server processed the request.

The client retries.

You could accidentally create:

``` text
Payment #1
Payment #2
```

For important operations, backend APIs may support idempotency keys.

``` text
request
+
Idempotency-Key: abc123
```

Then repeated requests can represent the same logical operation.

This is primarily a backend/API design concern, but frontend engineers
need to understand it when building reliable clients.

------------------------------------------------------------------------

# 151. Optimizing React rendering

Performance debugging should begin with measurement.

Do not immediately add:

``` text
useMemo
useCallback
memo
```

Instead:

``` text
slow UI
  ↓
measure
  ↓
find bottleneck
  ↓
identify cause
  ↓
optimize
  ↓
measure again
```

------------------------------------------------------------------------

# 152. Common rendering bottlenecks

``` text
Huge component tree
Large lists
Expensive calculations
Unnecessary context updates
Unstable props
Large bundles
Too many network requests
Expensive images
Repeated layout work
```

The solution depends on the bottleneck.

------------------------------------------------------------------------

# 153. Large lists

Rendering:

``` text
10,000 DOM nodes
```

can be expensive.

Virtualization renders only the visible portion:

``` text
10,000 records
      │
      ▼
┌──────────────────────┐
│ visible viewport      │
│ Item 100              │
│ Item 101              │
│ Item 102              │
│ ...                   │
│ Item 130              │
└──────────────────────┘
```

Libraries such as TanStack Virtual can solve this class of problem.

------------------------------------------------------------------------

# 154. Code splitting

A production bundle should not necessarily contain every feature
upfront.

``` text
main bundle
    │
    ├── core
    └── common UI

lazy chunk
    ├── admin
    ├── reports
    └── editor
```

Use dynamic imports:

``` js
const Reports = lazy(
    () => import("./Reports")
);
```

This reduces initial JavaScript.

------------------------------------------------------------------------

# 155. Image performance

Images can dominate page weight.

Production practices:

``` text
responsive images
modern formats
lazy loading
correct dimensions
compression
CDN
thumbnails
optimized delivery
```

Always specify dimensions where appropriate to reduce layout shifts.

------------------------------------------------------------------------

# 156. Core Web Vitals mindset

User-perceived performance includes:

``` text
loading
interaction responsiveness
visual stability
```

Do not measure only:

``` text
"React rendered in 20 ms"
```

Measure the actual page experience.

Important concepts include:

-   Largest Contentful Paint
-   Interaction to Next Paint
-   Cumulative Layout Shift

------------------------------------------------------------------------

# 157. Testing pyramid

A production React application can use:

``` text
                 E2E
                /   \
               /     \
         Integration
            /     \
           /       \
        Unit / component
```

Generally:

``` text
many fast tests
     ↓
fewer integration tests
     ↓
fewer expensive E2E tests
```

The exact ratio is project-dependent.

------------------------------------------------------------------------

# 158. Unit tests

Test pure logic:

``` text
price calculation
validation
formatters
reducers
mappers
utility functions
```

Example:

``` js
expect(calculateTotal(items)).toBe(120);
```

These tests should be fast and deterministic.

------------------------------------------------------------------------

# 159. Component/integration tests

Test user-visible behavior.

Prefer:

``` text
render
→ click
→ type
→ submit
→ assert UI
```

rather than testing implementation details such as:

``` text
"did setCount get called?"
```

A strong principle:

> Test what the user can observe and what the application contract
> guarantees.

------------------------------------------------------------------------

# 160. Vitest

Vitest is a modern JavaScript/TypeScript test runner commonly used with
Vite projects.

Typical uses:

``` text
unit tests
component tests
mocking
coverage
test projects
```

Conceptual structure:

``` text
source
  │
  ├── function
  ├── component
  └── hook
       │
       ▼
     Vitest
       │
       ▼
   assertions
```

------------------------------------------------------------------------

# 161. React Testing Library

React Testing Library encourages testing through the UI the way a user
interacts with it.

Prefer:

``` js
screen.getByRole("button", {
    name: "Submit"
});
```

over relying heavily on:

``` js
component.internalState
```

Good test:

``` text
user clicks Login
      ↓
request
      ↓
loading state
      ↓
success UI
```

This validates behavior rather than implementation.

------------------------------------------------------------------------

# 162. Mock Service Worker

Mocking API calls can be done at the network boundary.

Mental model:

``` text
React
  ↓
HTTP request
  ↓
MSW intercepts
  ↓
mock response
  ↓
React
```

This is often better than mocking every internal API function because
the test exercises more of the actual application path.

------------------------------------------------------------------------

# 163. Playwright

Playwright provides browser-based end-to-end testing.

Conceptually:

``` text
Playwright
    │
    ▼
real browser
    │
    ▼
your application
    │
    ▼
real DOM + network behavior
```

It supports Chromium, Firefox, and WebKit and provides features such as
auto-waiting, assertions, tracing, and parallel test execution.

------------------------------------------------------------------------

# 164. E2E test example

A production login test:

``` text
open application
      ↓
go to /login
      ↓
fill email
      ↓
fill password
      ↓
click Login
      ↓
wait for navigation/state
      ↓
assert dashboard visible
```

This is much closer to a real user journey than a unit test.

------------------------------------------------------------------------

# 165. Testing API behavior

Playwright can also make API requests directly.

Useful flow:

``` text
API setup
   ↓
browser test
   ↓
UI interaction
   ↓
API verification
```

This is valuable when preparing server-side state before a browser
scenario.

------------------------------------------------------------------------

# 166. Test data

Avoid tests that depend on random shared production-like state.

Use:

``` text
fixtures
factories
seed data
mock servers
test databases
isolated environments
```

A test should ideally be repeatable:

``` text
run 1 → pass
run 2 → pass
run 100 → pass
```

------------------------------------------------------------------------

# 167. CI/CD

A production frontend should not rely on:

``` text
developer remembers to run tests
```

Instead:

``` text
git push
    ↓
CI
    │
    ├── install
    ├── lint
    ├── typecheck
    ├── unit tests
    ├── build
    └── E2E
          │
          ▼
       deploy
```

------------------------------------------------------------------------

# 168. GitHub Actions example pipeline

``` text
Pull Request
     │
     ▼
┌──────────────┐
│ Install      │
└──────┬───────┘
       ▼
┌──────────────┐
│ Lint         │
└──────┬───────┘
       ▼
┌──────────────┐
│ Typecheck    │
└──────┬───────┘
       ▼
┌──────────────┐
│ Unit tests   │
└──────┬───────┘
       ▼
┌──────────────┐
│ Build        │
└──────┬───────┘
       ▼
┌──────────────┐
│ E2E tests    │
└──────┬───────┘
       ▼
    Deploy
```

------------------------------------------------------------------------

# 169. Linting

ESLint catches classes of mistakes and enforces conventions.

Examples:

``` text
unused variables
invalid Hooks usage
bad imports
unsafe patterns
inconsistent code
```

React-specific lint rules are particularly valuable because Hooks have
semantic constraints.

------------------------------------------------------------------------

# 170. Formatting

Use a formatter such as Prettier if your project chooses it.

The principle:

``` text
developer style
      ↓
formatter
      ↓
consistent source
```

Do not spend team time debating:

``` text
single quotes vs double quotes
spacing
line wrapping
```

automate it.

------------------------------------------------------------------------

# 171. TypeScript

For production React applications, TypeScript is strongly worth
learning.

It gives:

``` text
component prop types
API models
form types
Redux types
query types
event types
ref types
function contracts
```

But remember:

``` text
TypeScript
=
compile-time


Zod/runtime schemas
=
runtime
```

They solve different problems.

------------------------------------------------------------------------

# 172. Strict TypeScript

Use strict TypeScript when possible.

Important settings include:

``` json
{
    "compilerOptions": {
        "strict": true
    }
}
```

Strict typing exposes problems earlier.

Avoid defeating it with:

``` ts
any
```

everywhere.

------------------------------------------------------------------------

# 173. Avoid the `any` escape hatch

Bad:

``` ts
const response: any = await api.get(...);
```

Now TypeScript cannot help you.

Better:

``` ts
const response: Product = ...
```

or validate runtime data:

``` ts
const product = ProductSchema.parse(response.data);
```

The strongest boundary can be:

``` text
unknown
  ↓
runtime validation
  ↓
known type
```

------------------------------------------------------------------------

# 174. Type narrowing

TypeScript should model actual runtime possibilities.

Instead of:

``` ts
const error: any
```

prefer:

``` ts
if (error instanceof Error) {
    console.log(error.message);
}
```

For API/domain errors, define discriminated unions.

``` ts
type Result =
    | { ok: true; data: Product }
    | { ok: false; error: AppError };
```

This makes impossible states harder to represent.

------------------------------------------------------------------------

# 175. State machines

Some UI flows become difficult with booleans:

``` js
isLoading
isError
isSuccess
isSubmitting
isRetrying
```

You can accidentally create impossible states:

``` text
isLoading = true
isSuccess = true
isError = true
```

A state-machine mindset gives:

``` text
idle
 ↓
loading
 ├── success
 └── error
       ↓
     retry
       ↓
    loading
```

You do not always need a state-machine library.

But the modeling idea is extremely valuable.

------------------------------------------------------------------------

# 176. URL state

Not all state belongs in React state.

Search:

``` text
/products?search=react&page=2
```

is state.

It can be useful to put state in the URL when it should be:

-   bookmarkable
-   shareable
-   browser-history-aware
-   refresh-persistent

Examples:

``` text
search
filters
sort
pagination
selected tab
```

------------------------------------------------------------------------

# 177. Local storage and persistence

Some state needs persistence:

``` text
theme
language
draft
preferences
```

But avoid persisting everything.

Mental model:

``` text
React state
   ↓
change
   ↓
persistence layer
   ↓
localStorage / IndexedDB / server
```

Be careful with sensitive information.

------------------------------------------------------------------------

# 178. IndexedDB

For larger browser-side datasets, `localStorage` is not always
appropriate.

IndexedDB supports structured client-side storage.

Useful for:

``` text
offline apps
large cached datasets
drafts
client-side databases
```

A library can simplify its asynchronous API.

------------------------------------------------------------------------

# 179. Offline-first applications

An offline-capable application needs:

``` text
cache
+
local persistence
+
network detection
+
sync
+
conflict resolution
```

This is much more complex than:

``` js
if (!navigator.onLine) ...
```

True offline support is an architecture, not a boolean.

------------------------------------------------------------------------

# 180. Analytics

Production applications often measure:

``` text
page views
conversion
feature usage
errors
performance
funnels
```

Keep analytics calls out of random components where possible.

Create an abstraction:

``` ts
analytics.track("checkout_started", {
    cartSize: 3
});
```

This makes providers replaceable.

Never send secrets or unnecessary personal data to analytics.

------------------------------------------------------------------------

# 181. Error monitoring

A production application needs visibility into unexpected failures.

Conceptually:

``` text
User
 ↓
React error
 ↓
error boundary / global handler
 ↓
monitoring service
 ↓
stack trace
 ↓
release/version
 ↓
developer
```

Error monitoring should include enough context to reproduce problems
without leaking sensitive data.

------------------------------------------------------------------------

# 182. Logging

Avoid:

``` js
console.log(userPassword);
```

or:

``` js
console.log(accessToken);
```

Production logging should be:

``` text
structured
useful
redacted
correlated
environment-aware
```

A useful log can include:

``` text
request ID
route
operation
status
duration
error category
release version
```

------------------------------------------------------------------------

# 183. Request correlation

Distributed applications often need:

``` text
Frontend request
      │
      │ X-Request-ID: abc123
      ▼
Backend
      │
      ▼
database/service calls
```

Then logs across systems can be connected.

This is extremely useful when debugging production failures.

------------------------------------------------------------------------

# 184. Observability

A mature system has:

``` text
Logs
Metrics
Traces
Errors
```

Frontend observability can include:

``` text
page performance
API latency
JavaScript errors
failed requests
user-visible failures
release version
```

------------------------------------------------------------------------

# 185. Source maps

Production JavaScript is often minified:

``` text
main.a81f2.js
```

A stack trace without source maps is difficult to understand.

Source maps map:

``` text
compiled/minified JS
        ↓
original source
```

Store/protect source maps according to your security policy. They can
expose source information if publicly accessible.

------------------------------------------------------------------------

# 186. Bundle analysis

If your bundle grows:

``` text
500 KB
→
1 MB
→
2 MB
```

find out why.

Bundle analyzers can show:

``` text
React
chart library
editor
moment-like library
icons
duplicate dependencies
```

Then optimize:

``` text
tree shaking
dynamic imports
dependency replacement
lazy loading
```

------------------------------------------------------------------------

# 187. Dependency management

Every dependency adds:

``` text
bundle cost
security risk
maintenance cost
API learning cost
upgrade work
```

Before installing a library ask:

``` text
Can the platform solve this?
Can React solve this?
Can a small utility solve this?
Is the problem large enough for a dependency?
Is the dependency maintained?
Is the bundle cost acceptable?
```

------------------------------------------------------------------------

# 188. HTTP caching

Browser/server/CDN caching can dramatically improve performance.

Conceptually:

``` text
Browser
   │
   ├── cached response → instant
   │
   └── no/expired cache → network
```

Important HTTP concepts:

``` text
Cache-Control
ETag
Last-Modified
max-age
stale-while-revalidate
```

This is separate from React Query/RTK Query cache.

There can be multiple cache layers:

``` text
React query cache
      ↓
browser HTTP cache
      ↓
CDN cache
      ↓
backend cache
      ↓
database
```

------------------------------------------------------------------------

# 189. CDN

Static frontend assets are commonly served through a CDN:

``` text
User in India
      │
      ▼
nearby CDN edge
      │
      ▼
cached JS/CSS/images
```

This reduces latency.

------------------------------------------------------------------------

# 190. Deployment architecture

A typical SPA deployment:

``` text
                    USER
                      │
                      ▼
                    CDN
                 ┌────┴────┐
                 ▼         ▼
              HTML      assets
                           │
                           ▼
                         React
                           │
                           ▼
                         API
```

The frontend and backend may be deployed separately.

------------------------------------------------------------------------

# 191. SPA fallback

A client-side route:

``` text
/products/42
```

may not exist as a physical file on the server.

The server/CDN must often return the SPA entry HTML for application
routes.

Otherwise:

``` text
refresh /products/42
       ↓
server looks for file
       ↓
404
```

This is a classic SPA deployment problem.

------------------------------------------------------------------------

# 192. HTTPS

Production applications should use HTTPS.

HTTPS provides:

``` text
encryption
integrity
server authentication
```

Never design authentication around plain HTTP in production.

------------------------------------------------------------------------

# 193. Security headers

Production web applications can benefit from correctly configured
headers such as:

``` text
Content-Security-Policy
Strict-Transport-Security
X-Content-Type-Options
Referrer-Policy
Permissions-Policy
```

Exact configuration should match the application and deployment
environment.

Do not blindly paste a security-header configuration without
understanding what it breaks.

------------------------------------------------------------------------

# 194. Content Security Policy

CSP can restrict:

``` text
where scripts can load from
where images can load from
where connections can go
```

Conceptually:

``` text
Browser
  │
  ▼
CSP policy
  │
  ├── allowed
  └── blocked
```

It is an important defense-in-depth mechanism against some XSS classes.

------------------------------------------------------------------------

# 195. Internationalization

Production apps may require:

``` text
English
Kannada
Hindi
German
...
```

Internationalization affects:

``` text
translations
dates
numbers
currency
pluralization
text direction
layout
```

Do not build strings like:

``` js
`${count} item(s)`
```

if the application will support real localization.

------------------------------------------------------------------------

# 196. Time zones

Dates are a classic production bug.

Never casually assume:

``` text
new Date()
```

means the same calendar date for every user.

Think in:

``` text
Instant
   ↓
timezone
   ↓
localized display
```

Store/transfer timestamps consistently, commonly as UTC/ISO
representations, and localize at the UI boundary.

------------------------------------------------------------------------

# 197. Currency

Do not format currency manually:

``` js
"$" + price
```

Use appropriate internationalization APIs/libraries.

Currency depends on:

``` text
currency
locale
fraction digits
rounding
```

------------------------------------------------------------------------

# 198. Accessibility + internationalization + responsive design

A production component should survive:

``` text
desktop
mobile
keyboard
screen reader
long text
different languages
slow network
loading
error
empty state
```

This is what "production UI" actually means.

------------------------------------------------------------------------

# 199. Responsive design

Do not design only for:

``` text
1920 × 1080
```

Think:

``` text
mobile
tablet
desktop
wide desktop
```

Prefer responsive CSS/layout rather than reading viewport width into
React state unless JavaScript behavior genuinely needs viewport
information.

CSS should solve CSS problems where possible.

------------------------------------------------------------------------

# 200. Browser storage choices

``` text
Cookies
───────
HTTP requests / sessions / auth


localStorage
────────────
small persistent key/value data


sessionStorage
──────────────
tab/session scoped data


IndexedDB
─────────
larger structured browser data
```

Choose based on security and persistence requirements.

------------------------------------------------------------------------

# 201. Clipboard, notifications, geolocation and browser APIs

React does not replace browser APIs.

Examples:

``` js
navigator.clipboard
navigator.geolocation
Notification
IntersectionObserver
ResizeObserver
WebSocket
Web Workers
```

React's role is usually:

``` text
Effect/ref/event handler
       ↓
browser API
       ↓
result
       ↓
state/store
       ↓
render
```

------------------------------------------------------------------------

# 202. IntersectionObserver

Useful for:

``` text
infinite scrolling
lazy loading
visibility tracking
analytics
animations
```

Instead of continuously listening to scroll:

``` text
scroll event
scroll event
scroll event
...
```

the browser can notify you when an element intersects a viewport/root.

------------------------------------------------------------------------

# 203. Web Workers

Heavy JavaScript computation can block the main thread.

A Web Worker provides another execution context.

``` text
MAIN THREAD
    │
    ├── React
    ├── DOM
    └── user interaction
          │
          │ message
          ▼
      WEB WORKER
          │
          └── heavy computation
```

Use this for truly CPU-heavy tasks.

React itself does not move component rendering into Web Workers.

------------------------------------------------------------------------

# 204. Web Workers vs React concurrency

These are different.

``` text
React scheduling
────────────────
organizes React work


Web Worker
──────────
executes JavaScript in another worker context
```

React concurrency does not mean your component function executes on
another thread.

------------------------------------------------------------------------

# 205. Web performance: main-thread budget

The browser main thread is responsible for important work.

If you run:

``` js
while (true) {}
```

the page becomes unusable.

The same principle applies to expensive synchronous work.

Therefore:

``` text
Keep urgent main-thread work short.
Move huge computation elsewhere when appropriate.
```

------------------------------------------------------------------------

# 206. Memory leaks

Common frontend leaks:

``` text
forgotten event listener
forgotten interval
forgotten WebSocket
subscription without cleanup
unbounded cache
huge retained DOM references
```

React cleanup:

``` jsx
useEffect(() => {
    const id = setInterval(...);

    return () => clearInterval(id);
}, []);
```

Production debugging should include memory profiling for long-lived
applications.

------------------------------------------------------------------------

# 207. Race conditions

Frontend race conditions commonly appear in:

``` text
search
navigation
authentication refresh
autosave
uploads
mutations
```

Always ask:

> What if an older operation finishes after a newer operation?

Solutions:

``` text
cancellation
request identity
sequence numbers
cache keys
last-write-wins policy
server-side versioning
```

------------------------------------------------------------------------

# 208. Autosave

A production autosave flow might be:

``` text
user types
   ↓
local form state
   ↓
debounce
   ↓
save mutation
   ↓
server
   ↓
saved timestamp
```

Need to handle:

``` text
saving
saved
failed
retrying
offline
conflict
```

------------------------------------------------------------------------

# 209. Draft persistence

For long forms:

``` text
user types
   ↓
form state
   ↓
local persistence
   ↓
browser storage
```

On reload:

``` text
load draft
   ↓
validate
   ↓
populate form
```

Never blindly trust persisted data; it can become stale or malformed.

------------------------------------------------------------------------

# 210. API pagination + caching + filters

A real product list can combine:

``` text
/search
/filter
/sort
/pagination
```

Example query identity:

``` text
[
  "products",
  {
    search: "phone",
    category: "electronics",
    sort: "price",
    page: 2
  }
]
```

That identity can drive server-state caching.

------------------------------------------------------------------------

# 211. URL + server state architecture

A powerful production pattern:

``` text
URL state
   │
   ├── search
   ├── filters
   ├── sort
   └── page
   │
   ▼
query key
   │
   ▼
server state cache
   │
   ▼
UI
```

Now:

``` text
refresh
share URL
browser back
forward
```

all behave naturally.

------------------------------------------------------------------------

# 212. Authorization-aware UI

If the user lacks permission:

``` text
Backend
  ↓
403
```

The UI can hide/disable unavailable actions.

But:

``` text
hide button
      ≠
security
```

The backend must enforce permission.

------------------------------------------------------------------------

# 213. Role-based vs permission-based access

Role:

``` text
ADMIN
```

Permission:

``` text
products.delete
```

A permission-based model often scales better:

``` text
ADMIN
 ├── products.read
 ├── products.write
 ├── products.delete
 └── users.manage
```

Frontend can use permissions for UX.

Backend remains authoritative.

------------------------------------------------------------------------

# 214. Production application lifecycle

A realistic application lifecycle:

``` text
1. Requirement
       ↓
2. UI/domain design
       ↓
3. API contract
       ↓
4. Component architecture
       ↓
5. Forms + validation
       ↓
6. Data fetching/cache
       ↓
7. Authentication/authorization
       ↓
8. Error/loading/empty states
       ↓
9. Accessibility
       ↓
10. Tests
       ↓
11. Performance
       ↓
12. Security review
       ↓
13. CI
       ↓
14. Build
       ↓
15. Deploy
       ↓
16. Monitoring
       ↓
17. Iterate
```

------------------------------------------------------------------------

# 215. A production feature example

Imagine:

> Product creation page.

A mature architecture might be:

``` text
ProductCreatePage
        │
        ▼
ProductForm
        │
        ├── React Hook Form
        │
        ├── Zod schema
        │
        └── UI components
                │
                ▼
           submit()
                │
                ▼
         product mutation
                │
                ▼
          products API
                │
                ▼
             Axios
                │
                ▼
             Backend
                │
          ┌─────┴─────┐
          ▼           ▼
       success       error
          │           │
          ▼           ▼
invalidate      field/server
products        error mapping
          │
          ▼
query cache refresh
          │
          ▼
        UI
```

That is what "React development" becomes in a real application.

------------------------------------------------------------------------

# 216. Production feature checklist

For every feature ask:

``` text
□ UI component
□ Loading state
□ Empty state
□ Error state
□ Success state
□ API layer
□ Runtime validation
□ TypeScript types
□ Authentication requirement
□ Authorization requirement
□ Form validation
□ Accessibility
□ Mobile/responsive behavior
□ Tests
□ Analytics if required
□ Error monitoring
□ Cache/invalidation strategy
□ Retry/cancellation strategy
□ Security review
```

------------------------------------------------------------------------

# 217. Production page checklist

Every important page should answer:

``` text
What happens when:
──────────────────

□ data is loading?
□ data is empty?
□ API fails?
□ user is unauthorized?
□ user is forbidden?
□ network disappears?
□ user refreshes?
□ user presses Back?
□ user has slow internet?
□ user has keyboard only?
□ screen reader is used?
□ mobile width is tiny?
□ text is translated?
□ API returns unexpected data?
```

This mindset separates tutorial projects from production software.

------------------------------------------------------------------------

# 218. Recommended production React stack

There is no single mandatory stack.

A strong modern choice for many applications is:

``` text
Language
────────
TypeScript


Build
─────
Vite


UI
──
React


Routing
───────
React Router
or framework router


Forms
──────
React Hook Form


Validation
──────────
Zod


HTTP
────
fetch
or Axios


Server state
────────────
TanStack Query
or RTK Query


Client/global state
───────────────────
React state / Context
or Redux Toolkit


Styling
───────
CSS / Tailwind / design system


Testing
───────
Vitest + React Testing Library
+
Playwright


Mocking
───────
MSW


CI
──
GitHub Actions or equivalent


Deployment
──────────
CDN/static host or framework platform


Monitoring
──────────
error tracking + logs + analytics
```

You do **not** need all of these in every project.

------------------------------------------------------------------------

# 219. A simpler stack for a normal CRUD project

For a medium project:

``` text
React
TypeScript
Vite
React Router
React Hook Form
Zod
Axios
TanStack Query
Tailwind
Vitest
React Testing Library
Playwright
```

Add Redux Toolkit only if the application actually has substantial
shared client state.

------------------------------------------------------------------------

# 220. A Redux-heavy application

For a complex application:

``` text
React
TypeScript
Vite
React Router
Redux Toolkit
React-Redux
RTK Query
React Hook Form
Zod
Axios/fetch
Tailwind/design system
Vitest
Playwright
```

Remember:

``` text
RTK Query
=
server state/data fetching


Redux slices
=
client/application state
```

Do not put every server response into arbitrary Redux slices if RTK
Query already models it.

------------------------------------------------------------------------

# 221. A framework application

For a framework such as Next.js:

``` text
Framework
├── routing
├── server rendering
├── server components
├── client components
├── caching/data conventions
└── deployment integration

React
└── component model
```

The exact architecture differs from a pure Vite SPA.

Do not blindly apply SPA assumptions to a server-rendered framework.

------------------------------------------------------------------------

# 222. Library selection decision tree

Before adding a package:

``` text
Need to solve a problem
        │
        ▼
Can browser/platform solve it?
   /              \
 yes               no
 │                  │
use platform       ▼
                 Can React solve it?
                  /          \
                yes           no
                │              │
             React API        ▼
                           Is library
                           justified?
```

Then evaluate:

``` text
maintenance
security
bundle size
documentation
community
license
API quality
TypeScript support
testing
upgrade path
```

------------------------------------------------------------------------

# 223. What you should NOT put in React state

Do not store derived data unnecessarily.

Bad:

``` js
const [firstName, setFirstName] = useState("");
const [lastName, setLastName] = useState("");
const [fullName, setFullName] = useState("");
```

Better:

``` js
const fullName = `${firstName} ${lastName}`;
```

State should represent data that must persist/change independently.

Derived values should usually be calculated from existing state.

------------------------------------------------------------------------

# 224. Avoid unnecessary Effects

Bad conceptual pattern:

``` jsx
const [fullName, setFullName] = useState("");

useEffect(() => {
    setFullName(firstName + lastName);
}, [firstName, lastName]);
```

Better:

``` jsx
const fullName = firstName + lastName;
```

Why?

Because:

``` text
firstName/lastName
       ↓
render
       ↓
fullName
```

is pure derivation.

It does not require synchronization with an external system.

------------------------------------------------------------------------

# 225. Avoid state duplication

Bad:

``` text
products
selectedProduct
```

where `selectedProduct` duplicates an entire product object that already
exists in `products`.

Better:

``` text
products
selectedProductId
```

Then derive:

``` js
const selectedProduct =
    products.find(p => p.id === selectedProductId);
```

This avoids two sources of truth.

------------------------------------------------------------------------

# 226. Single source of truth

A common production principle:

``` text
ONE FACT
   ↓
ONE AUTHORITATIVE STATE
```

Avoid:

``` text
Redux state
+
Context state
+
local state
+
duplicated derived state
```

all representing the same fact.

Synchronization bugs grow rapidly with duplicated state.

------------------------------------------------------------------------

# 227. State ownership

Ask:

> Which component actually owns this state?

If only one component needs it:

``` text
local state
```

If sibling components need it:

``` text
lift state up
```

If many descendants need it:

``` text
Context / appropriate state architecture
```

If application-wide client state is complex:

``` text
Redux or another state manager
```

If it is server data:

``` text
server-state library
```

------------------------------------------------------------------------

# 228. The production state map

``` text
                    DATA
                      │
       ┌──────────────┼──────────────┐
       ▼              ▼              ▼
   UI/local        Server          URL
    state           state          state
       │              │              │
       ▼              ▼              ▼
 useState/         Query          Router
 reducer           cache
       │              │
       ▼              ▼
 Context/Redux     API
```

This is one of the most useful architecture diagrams for React
applications.

------------------------------------------------------------------------

# 229. Production request lifecycle

``` text
USER ACTION
    │
    ▼
React component
    │
    ▼
form validation
    │
    ▼
mutation/query
    │
    ▼
API function
    │
    ▼
Axios/fetch
    │
    ▼
request interceptor
    │
    ▼
HTTP
    │
    ▼
backend
    │
    ▼
response
    │
    ▼
response interceptor
    │
    ▼
runtime schema validation
    │
    ▼
query/cache/store
    │
    ▼
React
    │
    ▼
render
    │
    ▼
commit
    │
    ▼
browser
```

------------------------------------------------------------------------

# 230. Production form lifecycle

``` text
USER
 │
 ▼
input
 │
 ▼
React Hook Form
 │
 ├── field state
 ├── dirty/touched
 └── validation
       │
       ▼
     Zod
       │
   ┌───┴────┐
   ▼        ▼
invalid    valid
   │        │
   ▼        ▼
errors     submit
            │
            ▼
         mutation
            │
       ┌────┴─────┐
       ▼          ▼
    success      error
       │          │
       ▼          ▼
  invalidate    map server
    cache       errors
```

------------------------------------------------------------------------

# 231. Production authentication lifecycle

``` text
APPLICATION START
       │
       ▼
restore/check session
       │
       ▼
authenticated?
    /       \
  yes        no
   │          │
   ▼          ▼
  APP        LOGIN
   │
   ▼
API request
   │
   ▼
401?
 /   \
no    yes
│      │
▼      ▼
data   refresh
       │
    success?
     /     \
   yes      no
   │         │
retry       logout
request
```

------------------------------------------------------------------------

# 232. Production deployment lifecycle

``` text
Developer
    │
    ▼
Git commit
    │
    ▼
Pull Request
    │
    ▼
CI
 ┌──┼───────────────┐
 │  │               │
lint tests       typecheck
 │  │               │
 └──┼───────────────┘
    ▼
build
    │
    ▼
artifact
    │
    ▼
staging
    │
    ▼
E2E / smoke tests
    │
    ▼
production
    │
    ▼
monitoring
    │
    ▼
feedback
```

------------------------------------------------------------------------

# 233. The complete production architecture

``` text
┌──────────────────────────────────────────────────────────────────────┐
│                             BROWSER                                  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                         REACT APP                              │  │
│  │                                                                │  │
│  │  Components ─ Composition ─ Hooks ─ Context                    │  │
│  │       │                                                        │  │
│  │       ├── Forms ── React Hook Form ── Zod                     │  │
│  │       │                                                        │  │
│  │       ├── Router                                               │  │
│  │       │                                                        │  │
│  │       ├── Local State / Redux Toolkit                          │  │
│  │       │                                                        │  │
│  │       └── Query / Server State                                 │  │
│  │                         │                                      │  │
│  │                         ▼                                      │  │
│  │                   API / HTTP layer                             │  │
│  │                         │                                      │  │
│  │                   Axios / fetch                                │  │
│  │                         │                                      │  │
│  └─────────────────────────┼──────────────────────────────────────┘  │
│                            │                                         │
└────────────────────────────┼─────────────────────────────────────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │    BACKEND API   │
                    └────────┬─────────┘
                             │
                      auth / database
                             │
                             ▼
                       SERVER SYSTEM
```

Around the entire system:

``` text
┌─────────────────────────────────────────────────────────────────────┐
│ QUALITY                                                             │
│ TypeScript • ESLint • Vitest • RTL • Playwright • MSW              │
├─────────────────────────────────────────────────────────────────────┤
│ SECURITY                                                            │
│ Auth • Authorization • CORS • CSRF • XSS • CSP • Secrets           │
├─────────────────────────────────────────────────────────────────────┤
│ PERFORMANCE                                                         │
│ Code splitting • caching • virtualization • images • Web Vitals    │
├─────────────────────────────────────────────────────────────────────┤
│ OPERATIONS                                                          │
│ CI/CD • logs • monitoring • analytics • error tracking             │
└─────────────────────────────────────────────────────────────────────┘
```

------------------------------------------------------------------------

# 234. What is actually required for YOUR project?

Do not blindly install 30 packages.

For a normal full-stack project, start with:

``` text
React
TypeScript
Vite
React Router
React Hook Form
Zod
Axios or fetch
TanStack Query OR RTK Query
Tailwind/CSS
Vitest
React Testing Library
Playwright
ESLint
```

Then add only when the requirement appears:

``` text
Redux Toolkit
WebSocket library
file upload library
virtualization
i18n
analytics
error monitoring
advanced charts
rich text editor
drag/drop
offline storage
```

------------------------------------------------------------------------

# 235. The production learning roadmap

If your goal is to become a strong React developer, learn in this order:

``` text
PHASE 1
───────
Browser
JavaScript
DOM
events
async JavaScript
modules


PHASE 2
───────
React fundamentals
JSX
elements
components
props
composition


PHASE 3
───────
State
useState
snapshots
batching
updaters
identity
keys


PHASE 4
───────
Hooks
useEffect
useRef
useContext
useReducer
custom Hooks


PHASE 5
───────
Rendering
render
reconciliation
commit
Fiber
scheduling


PHASE 6
───────
Application React
forms
routing
data fetching
loading/error states


PHASE 7
───────
Production ecosystem
Axios/fetch
React Hook Form
Zod
TanStack Query/RTK Query
Redux Toolkit


PHASE 8
───────
Quality
TypeScript
testing
MSW
Playwright
linting
CI/CD


PHASE 9
───────
Production engineering
auth
security
performance
accessibility
observability
deployment


PHASE 10
────────
Advanced
concurrency
Suspense
server rendering
hydration
server components
React internals
```

------------------------------------------------------------------------

# 236. The "I can build a production app" checklist

You are ready to build a serious React project when you can
independently implement:

``` text
□ React/Vite setup
□ TypeScript
□ routing
□ reusable components
□ layout system
□ forms
□ validation
□ API client
□ authentication
□ protected routes
□ authorization-aware UI
□ CRUD
□ pagination
□ search
□ debounce
□ server-state caching
□ mutations
□ optimistic UI
□ file upload
□ loading/error/empty states
□ toast/notifications
□ modal/dialog
□ responsive UI
□ accessibility basics
□ global/client state
□ API error handling
□ request cancellation
□ tests
□ E2E tests
□ environment configuration
□ production build
□ CI pipeline
□ deployment
□ monitoring
```

------------------------------------------------------------------------

# 237. The most important engineering principle

A production React developer should stop thinking:

> "Which Hook should I use?"

and start asking:

> "What state exists, who owns it, where does it come from, who needs
> it, how long should it live, what can fail, and what does the user see
> during every transition?"

That leads naturally to the correct architecture.

------------------------------------------------------------------------

# 238. Final production mental model

``` text
                       USER
                         │
                         ▼
                 ┌───────────────┐
                 │      UI       │
                 └───────┬───────┘
                         │
                interaction/event
                         │
                         ▼
                 ┌───────────────┐
                 │     STATE     │
                 └───────┬───────┘
                         │
                         ▼
                 ┌───────────────┐
                 │     REACT     │
                 │               │
                 │ render        │
                 │ reconcile     │
                 │ commit        │
                 └───────┬───────┘
                         │
                         ▼
                    Browser DOM
                         │
                         ▼
                      SCREEN


For server data:

UI
 │
 ▼
Query / mutation
 │
 ▼
API client
 │
 ▼
HTTP
 │
 ▼
Backend
 │
 ▼
database/services
 │
 ▼
response
 │
 ▼
runtime validation
 │
 ▼
cache/store
 │
 ▼
React
```

The production system is therefore not:

``` text
React = entire frontend
```

It is:

``` text
React
+
browser platform
+
application architecture
+
networking
+
validation
+
state management
+
server-state management
+
routing
+
testing
+
security
+
performance
+
deployment
+
observability
```

And React remains the core UI engine inside that larger system.

------------------------------------------------------------------------

# 239. Production reference links

React: - https://react.dev/

Axios: - https://axios-http.com/

React Hook Form: - https://react-hook-form.com/

Zod: - https://zod.dev/

TanStack Query: - https://tanstack.com/query/

TanStack Form: - https://tanstack.com/form/

Redux: - https://redux.js.org/

Redux Toolkit: - https://redux-toolkit.js.org/

React Redux: - https://react-redux.js.org/

React Router: - https://reactrouter.com/

Vitest: - https://vitest.dev/

React Testing Library: - https://testing-library.com/react

Playwright: - https://playwright.dev/

MSW: - https://mswjs.io/

Vite: - https://vite.dev/

TypeScript: - https://www.typescriptlang.org/

MDN Web APIs: - https://developer.mozilla.org/en-US/docs/Web/API

Web Vitals: - https://web.dev/articles/vitals

------------------------------------------------------------------------

# 240. Final distinction: React developer vs production frontend engineer

A React developer who only knows:

``` text
useState
useEffect
useContext
useMemo
```

can write components.

A production frontend engineer understands:

``` text
browser
   +
React
   +
state
   +
network
   +
forms
   +
validation
   +
authentication
   +
security
   +
server state
   +
testing
   +
performance
   +
accessibility
   +
deployment
   +
observability
```

That is the complete skill map this README is intended to provide.
