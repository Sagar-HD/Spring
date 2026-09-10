# Redux Toolkit --- First Principles, From Context to RTK Query

> **Learning goal:** Understand Redux Toolkit so deeply that the APIs
> stop feeling like magic.
>
> The mental model used throughout this README is:
>
> **problem → primitive mechanism → manual implementation → pain →
> abstraction → what the abstraction does underneath → syntax**
>
> Assumption: you already know JavaScript deeply and understand React
> components, props, state, hooks, and Context API.

------------------------------------------------------------------------

## 0. The Entire Story in One Diagram

Redux Toolkit becomes much easier when you see it as a sequence of
problems.

``` text
React application
       │
       │ local state works
       ▼
Component state
       │
       │ "I need this data in distant components"
       ▼
Props / prop drilling
       │
       │ "I don't want to manually pass it through every level"
       ▼
Context API
       │
       │ solves distribution, but not full state architecture
       ▼
Redux
       │
       │ centralized state + actions + reducers + middleware
       ▼
Redux Toolkit
       │
       │ removes repetitive Redux boilerplate
       ├───────────────┐
       ▼               ▼
createSlice       configureStore
       │
       │ API calls introduce side effects
       ▼
Middleware
       │
       ├── custom API middleware
       │
       └── thunk
              │
              │ async lifecycle becomes repetitive
              ▼
       createAsyncThunk
              │
              │ server state becomes a separate problem:
              │ caching, stale data, refetching, deduplication...
              ▼
          RTK Query
              │
              ├── query/cache
              ├── mutations
              ├── invalidation/refetch
              ├── subscriptions
              └── optimistic updates + rollback
```

The important insight is:

> Redux Toolkit is not one giant feature. It is a collection of
> abstractions introduced to solve progressively different problems.

------------------------------------------------------------------------

# 1. Before Redux: What Problem Are We Actually Solving?

React already gives us state.

``` jsx
function Counter() {
  const [count, setCount] = useState(0)

  return (
    <button onClick={() => setCount(count + 1)}>
      {count}
    </button>
  )
}
```

The state belongs to the component.

``` text
Counter
 ├── count = 0
 └── setCount()
```

For local UI state, this is excellent.

Examples:

-   modal open/closed
-   input value
-   dropdown state
-   selected tab
-   hover state
-   local form state

The first question is therefore **not**:

> "Should I use Redux?"

It is:

> **"Who owns this state?"**

------------------------------------------------------------------------

# 2. React State and the Component Tree

Suppose:

``` text
App
├── Navbar
├── Dashboard
│   ├── BookList
│   └── BookDetails
└── Footer
```

Suppose `Navbar` needs the logged-in user's name and `BookDetails` also
needs it.

If the state lives in `App`:

``` text
App
 └── user
```

we can pass it:

``` jsx
<App>
  <Navbar user={user} />
  <Dashboard user={user} />
</App>
```

But now `Dashboard` may not actually need `user`. It is only passing it
further:

``` text
App
  │
  │ user
  ▼
Dashboard
  │
  │ user
  ▼
BookDetails
```

This is **prop drilling**.

The data is being passed through components that don't own or use it.

------------------------------------------------------------------------

# 3. Context API: What Does It Actually Solve?

Context says:

> "Instead of manually passing a value through every component, make the
> value available to descendants."

Example:

``` jsx
const UserContext = createContext(null)

function App() {
  const user = {
    name: "Sagar"
  }

  return (
    <UserContext.Provider value={user}>
      <Dashboard />
    </UserContext.Provider>
  )
}
```

Then:

``` jsx
function BookDetails() {
  const user = useContext(UserContext)

  return <h1>Hello {user.name}</h1>
}
```

No prop drilling.

But understand what Context actually is.

------------------------------------------------------------------------

# 4. Context Is a React Distribution Mechanism

Context does not magically create global state.

You still have a JavaScript value:

``` js
const user = {
  name: "Sagar"
}
```

The Provider says:

``` jsx
<UserContext.Provider value={user}>
  ...
</UserContext.Provider>
```

Conceptually:

``` text
                Provider
                   │
                   │ value = user
                   ▼
        ┌──────────────────────┐
        │     React subtree    │
        │                      │
        │   Dashboard          │
        │      │               │
        │      ▼               │
        │  BookDetails         │
        │      │               │
        │      ▼               │
        │  useContext()        │
        └──────────────────────┘
```

`useContext(UserContext)` asks React for the current value associated
with that context in the component's ancestry.

So Context solves:

> **How can a value be made available across a React subtree without
> prop drilling?**

It does **not** by itself provide:

-   action architecture
-   reducers
-   middleware
-   async workflows
-   server cache
-   request deduplication
-   mutation lifecycle
-   cache invalidation

That distinction matters.

------------------------------------------------------------------------

# 5. Context vs Redux

A useful mental distinction:

``` text
Context
   ↓
"How do I distribute this value?"

Redux
   ↓
"How do I architect and manage changing shared state?"
```

Context can distribute state created elsewhere.

Redux provides a structured state-management system:

``` text
Store
  │
  ├── State
  ├── Dispatch
  ├── Reducers
  └── Middleware
```

Redux itself can work without React. React-Redux is the bridge between
React and Redux.

------------------------------------------------------------------------

# 6. The Core Redux Idea

Imagine we have:

``` js
const state = {
  books: [],
  user: null
}
```

We want controlled state transitions.

Instead of components directly changing:

``` js
state.books.push(book)
```

we say:

> "Something happened."

That description is an **action**.

``` js
{
  type: "books/bookAdded",
  payload: book
}
```

Then a reducer decides what the new state should be.

``` js
function booksReducer(state, action) {
  if (action.type === "books/bookAdded") {
    return {
      ...state,
      books: [...state.books, action.payload]
    }
  }

  return state
}
```

The core flow:

``` text
Component
    │
    │ dispatch(action)
    ▼
  Redux Store
    │
    ▼
 Reducer
    │
    │ old state + action
    ▼
 New State
    │
    ▼
 React UI
```

Redux's fundamental rule is:

> The only way to change Redux state is by dispatching an action, which
> reducers process to calculate the next state.

Redux's official documentation describes the store as holding the global
state tree, actions as descriptions of what happened, and reducers as
pure functions that calculate the next state. [Redux Getting
Started](https://redux.js.org/introduction/getting-started)

------------------------------------------------------------------------

# 7. What Is the Store?

The store is the central container that holds the Redux state tree and
coordinates dispatching and subscriptions.

Conceptually:

``` text
Redux Store
┌───────────────────────────────┐
│                               │
│  state                        │
│  ├── auth                     │
│  ├── books                    │
│  ├── ui                       │
│  └── borrows                  │
│                               │
│  dispatch(action)             │
│  getState()                   │
│  subscribe(listener)          │
│                               │
└───────────────────────────────┘
```

Think of it as the application's centralized state machine.

------------------------------------------------------------------------

# 8. Reducer: The Decision-Maker

A reducer has a simple contract:

``` js
(state, action) => newState
```

Example:

``` js
function counterReducer(state = 0, action) {
  switch (action.type) {
    case "counter/increment":
      return state + 1

    default:
      return state
  }
}
```

The reducer does not call:

``` text
fetch()
axios()
database
random APIs
```

It receives:

``` text
old state
+
description of what happened
```

and calculates:

``` text
new state
```

This makes reducers predictable.

------------------------------------------------------------------------

# 9. Why Redux Uses Actions

Suppose three different things can add a book:

``` text
Admin UI
Import system
WebSocket event
```

Instead of directly manipulating state, each can describe:

``` js
{
  type: "books/bookAdded",
  payload: book
}
```

The reducer owns the rule:

``` text
bookAdded
    ↓
books state changes
```

This creates a traceable state transition:

``` text
User action
    ↓
Action
    ↓
Reducer
    ↓
State change
```

That traceability is one reason Redux has powerful debugging tools.

------------------------------------------------------------------------

# 10. The Problem with Raw Redux

Traditional Redux code requires manually creating:

-   action type strings
-   action creators
-   reducers
-   immutable update logic
-   store setup
-   middleware configuration

For example:

``` js
const ADD_BOOK = "books/addBook"

const addBook = (book) => ({
  type: ADD_BOOK,
  payload: book
})
```

Then:

``` js
function booksReducer(state = [], action) {
  switch (action.type) {
    case ADD_BOOK:
      return [...state, action.payload]

    default:
      return state
  }
}
```

It works.

But there is a lot of repetitive code.

This is where **Redux Toolkit** enters.

------------------------------------------------------------------------

# 11. Redux vs Redux Toolkit

This distinction is critical.

### Redux

The underlying state-management library / architecture.

### Redux Toolkit

The officially recommended way to write Redux logic today.

Redux Toolkit wraps Redux core and provides APIs for:

-   store setup
-   slices
-   immutable update logic
-   async workflows
-   selectors/utilities
-   RTK Query

The official Redux docs recommend Redux Toolkit as the standard way to
write Redux logic. [Redux Toolkit
Overview](https://redux.js.org/redux-toolkit/overview)

Think:

``` text
Redux
 └── core concepts

Redux Toolkit
 └── practical toolbox for using Redux correctly
```

RTK does not replace the Redux model.

It makes the model easier to use.

------------------------------------------------------------------------

# 12. `createSlice`: The First Big RTK Abstraction

Instead of separately writing:

``` text
action types
action creators
reducer
```

you define a slice:

``` js
const booksSlice = createSlice({
  name: "books",

  initialState: {
    books: []
  },

  reducers: {
    bookAdded: (state, action) => {
      state.books.push(action.payload)
    }
  }
})
```

From this one definition, RTK generates:

``` text
booksSlice
   │
   ├── reducer
   ├── action creators
   └── action types
```

So:

``` js
booksSlice.actions.bookAdded(book)
```

creates an action.

And:

``` js
booksSlice.reducer
```

is the reducer to put into the store.

------------------------------------------------------------------------

# 13. Where Did That "Mutation" Syntax Come From?

This is **Immer**.

Redux normally requires immutable updates.

Without Immer:

``` js
return {
  ...state,
  books: [
    ...state.books,
    action.payload
  ]
}
```

With Redux Toolkit:

``` js
state.books.push(action.payload)
```

This looks like mutation.

But RTK uses Immer internally.

Immer gives the reducer a draft state:

``` text
Real Redux state
       │
       ▼
     Immer
       │
       ▼
   draft state
       │
       │ your code appears to mutate it
       ▼
Immer calculates immutable changes
       │
       ▼
new Redux state
```

So:

``` js
state.books.push(book)
```

does **not** mean the original Redux state is being mutated.

Immer tracks the changes and creates the correct immutable next state.
Redux Toolkit's `createSlice` and `createReducer` use Immer internally.
[Redux Toolkit --- Immer
Reducers](https://redux-toolkit.js.org/usage/immer-reducers)

------------------------------------------------------------------------

# 14. Store Setup

Modern Redux Toolkit usually starts with:

``` js
const store = configureStore({
  reducer: {
    auth: authReducer,
    books: booksReducer
  }
})
```

Conceptually:

``` text
Redux Store
│
├── auth → authReducer
├── books → booksReducer
└── ...
```

`configureStore()` also provides sensible middleware configuration and
Redux DevTools integration. [Redux Toolkit Quick
Start](https://redux.js.org/tutorials/quick-start)

------------------------------------------------------------------------

# 15. React-Redux: The Bridge

Redux itself does not know about React components.

React-Redux connects them.

``` jsx
<Provider store={store}>
  <App />
</Provider>
```

The Provider makes the Redux store available to the React subtree.

Then:

``` js
const books = useSelector(state => state.books.books)
```

means:

> "Subscribe this component to this piece of Redux state."

And:

``` js
const dispatch = useDispatch()
```

gives the component access to:

``` js
store.dispatch
```

Conceptually:

``` text
                 Redux Store
                     │
          ┌──────────┴──────────┐
          │                     │
       dispatch              state
          │                     │
          ▼                     ▼
     useDispatch()         useSelector()
          │                     │
          ▼                     ▼
      Component             Component
```

------------------------------------------------------------------------

# 16. Selector and Why It Matters

A selector is simply a function that reads state:

``` js
const selectBooks = state => state.books.books
```

Then:

``` js
const books = useSelector(selectBooks)
```

You can also derive data:

``` js
const selectAvailableBooks = state =>
  state.books.books.filter(book => book.available)
```

But this creates a new array every time the selector runs.

For expensive derived data, memoized selectors using `createSelector`
can reuse the previous result when inputs have not changed.

Mental rule:

``` text
Reading existing reference
    → normal selector is usually enough

Creating derived object/array
    → consider memoization
```

Do not memoize every selector automatically.

------------------------------------------------------------------------

# 17. Now the Application Needs an API

Imagine your Library app.

You have:

``` text
GET    /borrow/my
POST   /borrow
DELETE /borrow/:id
```

The frontend needs data from the backend.

A beginner might do:

``` jsx
useEffect(() => {
  fetch("/borrow/my")
    .then(...)
}, [])
```

and then:

``` js
setBooks(...)
```

But now ask:

> Where does shared server data live?

If many components need it, you may put it in Redux.

So you might create:

``` text
Component
   ↓
API call
   ↓
response
   ↓
dispatch(successAction)
   ↓
Redux reducer
   ↓
Redux state
```

This is reasonable.

But now a problem appears.

------------------------------------------------------------------------

# 18. Component API Calls + Redux Storage

Suppose:

``` jsx
useEffect(() => {
  api.get("/borrow/my")
    .then(response => {
      dispatch(
        borrowedBooksLoaded(response.data)
      )
    })
}, [])
```

The component is now responsible for:

``` text
when to fetch
what URL to call
which API function to call
how to handle response
which action to dispatch
```

That mixes UI logic and data-fetching logic.

A service layer improves this:

``` js
// service
export const getMyBorrowedBooks = async () => {
  const response = await api.get("/borrow/my")
  return response.data
}
```

Then:

``` text
Component
    ↓
Service
    ↓
HTTP
    ↓
Backend
```

But you still need to connect the result to Redux.

------------------------------------------------------------------------

# 19. Action Creators

An action creator is simply a function whose job is to create an action
object.

``` js
const bookAdded = (book) => ({
  type: "books/bookAdded",
  payload: book
})
```

Then:

``` js
dispatch(bookAdded(book))
```

Think:

``` text
bookAdded(book)
      ↓
action object
      ↓
dispatch()
```

The action creator's job is **creating the description of what
happened**.

It is not supposed to perform the API call.

------------------------------------------------------------------------

# 20. Why Middleware Appears

Reducers must stay predictable.

We don't want:

``` js
function booksReducer(state, action) {
  fetch("/books") // ❌
}
```

API calls are side effects.

So Redux provides middleware as a pipeline between:

``` text
dispatch
   ↓
middleware
   ↓
reducer
```

Conceptually:

``` text
dispatch(action)
       ↓
Middleware 1
       ↓
Middleware 2
       ↓
Reducer
       ↓
new state
```

This is similar to a servlet filter chain:

``` text
Request
  ↓
Filter
  ↓
Filter
  ↓
Servlet
```

`next(action)` means:

> "Pass this action to the next stage."

------------------------------------------------------------------------

# 21. Custom API Middleware

You built the idea manually:

``` js
const apiMiddleware = store => next => action => {

  if (action.type === "api/makeRequest") {

    const {
      url,
      onSuccess,
      onError
    } = action.payload

    api.get(url)
      .then(response => {
        store.dispatch({
          type: onSuccess,
          payload: response.data
        })
      })
      .catch(error => {
        store.dispatch({
          type: onError,
          payload: error.message
        })
      })
  }

  return next(action)
}
```

Then the component could say:

``` js
dispatch({
  type: "api/makeRequest",

  payload: {
    url: "/borrow/my",
    onSuccess: "borrows/onSuccess",
    onError: "borrows/onError"
  }
})
```

The flow:

``` text
Component
   │
   │ dispatch(api/makeRequest)
   ▼
Redux
   │
   ▼
API Middleware
   │
   ├── api.get()
   │
   ▼
Backend
   │
   ├── success → dispatch(success)
   └── error   → dispatch(error)
                    │
                    ▼
                 Reducer
                    │
                    ▼
                 Redux State
```

You had full control.

And this is an excellent way to understand middleware.

------------------------------------------------------------------------

# 22. But Your Custom Middleware Had a Problem

The component still had to know:

``` text
URL
success action
error action
```

That leaks implementation details into the component.

Ideally the component should say:

``` js
dispatch(fetchMyBorrowedBooks())
```

rather than:

``` js
dispatch({
  type: "api/makeRequest",
  payload: {
    url: "/borrow/my",
    ...
  }
})
```

We want to describe **intent**, not HTTP implementation.

This leads to thunk.

------------------------------------------------------------------------

# 23. Thunk: The Problem It Solves

Normal Redux actions are objects:

``` js
{
  type: "books/add",
  payload: book
}
```

What if we dispatch a function?

``` js
dispatch(someFunction)
```

Redux core doesn't process functions as normal actions.

Thunk middleware intercepts them.

Conceptually:

``` js
const thunkMiddleware =
  store =>
  next =>
  action => {

    if (typeof action === "function") {
      return action(
        store.dispatch,
        store.getState
      )
    }

    return next(action)
  }
```

So:

``` text
dispatch(function)
       ↓
Thunk Middleware
       ↓
execute function
       ↓
function receives dispatch/getState
       ↓
API call
       ↓
dispatch normal actions
       ↓
reducers
```

Redux Toolkit's default middleware includes thunk support, so you
normally do not write this middleware yourself.

------------------------------------------------------------------------

# 24. Action Creator vs Thunk Creator

These are easy to confuse.

### Normal action creator

``` js
const addBook = (book) => ({
  type: "books/add",
  payload: book
})
```

Returns:

``` text
ACTION OBJECT
```

### Thunk creator

``` js
const fetchBooks = () => async (dispatch, getState) => {
  const books = await getBooks()

  dispatch({
    type: "books/loaded",
    payload: books
  })
}
```

Returns:

``` text
THUNK FUNCTION
```

Then:

``` js
dispatch(fetchBooks())
```

becomes:

``` text
dispatch(thunk function)
        ↓
thunk middleware
        ↓
execute thunk
        ↓
API
        ↓
dispatch(normal action)
```

------------------------------------------------------------------------

# 25. Why `createAsyncThunk`?

Thunk solved:

> "How can I execute async logic from Redux?"

But now every async operation needs repetitive lifecycle state:

``` js
loading
error
data
```

For example:

``` text
request starts
    ↓
loading = true
    ↓
request succeeds
    ↓
loading = false
data = response
```

or:

``` text
request starts
    ↓
loading = true
    ↓
request fails
    ↓
loading = false
error = ...
```

You can manually write this.

But it's repetitive.

------------------------------------------------------------------------

# 26. `createAsyncThunk`

RTK gives:

``` js
export const fetchMyBorrowedBooks =
  createAsyncThunk(
    "borrows/fetchMyBorrowedBooks",

    async () => {
      const data = await getMyBorrowedBooks()
      return data
    }
  )
```

The first string:

``` js
"borrows/fetchMyBorrowedBooks"
```

is the action type prefix.

RTK generates lifecycle actions:

``` text
borrows/fetchMyBorrowedBooks/pending
borrows/fetchMyBorrowedBooks/fulfilled
borrows/fetchMyBorrowedBooks/rejected
```

So conceptually:

``` text
createAsyncThunk
       │
       ├── thunk function
       │
       └── lifecycle action creators
              │
              ├── pending
              ├── fulfilled
              └── rejected
```

------------------------------------------------------------------------

# 27. `extraReducers`

The async thunk generates actions.

Your slice needs to react to them.

``` js
const borrowSlice = createSlice({
  name: "borrows",

  initialState: {
    borrowedBooks: [],
    loading: false,
    error: null
  },

  reducers: {},

  extraReducers: builder => {

    builder

      .addCase(
        fetchMyBorrowedBooks.pending,
        state => {
          state.loading = true
          state.error = null
        }
      )

      .addCase(
        fetchMyBorrowedBooks.fulfilled,
        (state, action) => {
          state.loading = false
          state.borrowedBooks = action.payload
        }
      )

      .addCase(
        fetchMyBorrowedBooks.rejected,
        (state, action) => {
          state.loading = false
          state.error = action.error.message
        }
      )
  }
})
```

Why `extraReducers`?

Because these actions were not created inside the slice's own `reducers`
section.

They were generated by `createAsyncThunk`.

So:

``` text
createAsyncThunk
     ↓
generates lifecycle actions
     ↓
slice listens through extraReducers
```

------------------------------------------------------------------------

# 28. Complete `createAsyncThunk` Flow

``` text
Component
   │
   │ dispatch(fetchMyBorrowedBooks())
   ▼
Thunk Middleware
   │
   ▼
createAsyncThunk logic
   │
   ├── dispatch pending
   │        ↓
   │     loading=true
   │
   ├── call service
   │        ↓
   │     backend
   │
   ├── success
   │        ↓
   │     fulfilled
   │        ↓
   │     data stored
   │
   └── failure
            ↓
         rejected
            ↓
         error stored
```

This solved async lifecycle management.

But now we encounter a **different** problem.

------------------------------------------------------------------------

# 29. The Big Distinction: Client State vs Server State

Consider:

``` js
const theme = "dark"
```

That is client state.

Your React application owns it.

But:

``` js
const books = [...]
```

came from the backend.

The backend is the authority.

This is **server state**.

Server state has special problems:

``` text
Where is the cache?
When is it stale?
Should I fetch again?
Can two components share the same response?
When should unused data be removed?
What happens after a mutation?
Should I refetch?
Can I show old data while fetching?
Can I update the cache optimistically?
```

`createAsyncThunk` can make the request.

It does not automatically solve this entire server-state lifecycle.

This is the problem RTK Query was designed for.

Official Redux documentation describes RTK Query as a data fetching and
caching tool built on Redux Toolkit, specifically intended to remove the
need to hand-write fetching and caching logic. [RTK Query
Overview](https://redux-toolkit.js.org/rtk-query/overview)

------------------------------------------------------------------------

# 30. Before RTK Query: The Manual Server-State Problem

Suppose:

``` text
GET /books
```

You fetch:

``` text
[A, B, C]
```

and store it.

Then user deletes B:

``` text
DELETE /books/B
```

The backend becomes:

``` text
[A, C]
```

But your frontend cache still says:

``` text
[A, B, C]
```

So you might do:

``` text
DELETE
   ↓
wait
   ↓
GET /books again
   ↓
[A, C]
   ↓
Redux state update
```

Now imagine slow network:

``` text
User clicks Delete
        ↓
DELETE takes 800ms
        ↓
GET takes another 800ms
        ↓
UI appears unchanged for 1.6 seconds
```

It feels laggy.

You could manually manipulate Redux state immediately, but then you have
to solve:

``` text
Which cached data changed?
What if request fails?
How do I rollback?
When should I refetch?
What if another component uses the same data?
How long should cache remain?
What if two components request the same resource?
```

This is a new category of problem.

------------------------------------------------------------------------

# 31. RTK Query's Mental Model

RTK Query says:

> **Treat server data as cached resources with lifecycles.**

Instead of manually doing:

``` text
fetch
store
cache
refetch
invalidate
subscribe
cleanup
```

you define the server interface.

``` js
export const booksApi = createApi({

  reducerPath: "booksApi",

  baseQuery: fetchBaseQuery({
    baseUrl: "/api"
  }),

  endpoints: builder => ({

    getBooks: builder.query({
      query: () => "/books"
    })

  })
})
```

You are declaring:

> "Here is a server resource called `getBooks`, and here is how to
> retrieve it."

------------------------------------------------------------------------

# 32. `createApi`

Think of:

``` js
createApi(...)
```

as creating an **API manager**.

You give it:

``` text
base HTTP mechanism
+
server endpoints
+
cache behavior
+
optional lifecycle behavior
```

Conceptually:

``` text
createApi()
    │
    ├── HTTP configuration
    ├── endpoint definitions
    ├── Redux state
    ├── middleware
    ├── cache management
    └── generated hooks
```

RTK Query's official docs describe `createApi()` as the core entry point
used to define endpoints and fetching/caching behavior. They generally
recommend one API slice per base URL. [RTK Query
Overview](https://redux-toolkit.js.org/rtk-query/overview)

------------------------------------------------------------------------

# 33. `baseQuery`

``` js
baseQuery: fetchBaseQuery({
  baseUrl: "/api"
})
```

This is the HTTP mechanism.

Think about your custom middleware:

``` js
api.get(url)
```

You chose Axios.

RTK Query commonly uses:

``` js
fetchBaseQuery(...)
```

which is a small wrapper around `fetch`.

So:

``` text
RTK Query
    ↓
baseQuery
    ↓
HTTP
    ↓
Backend
```

You can also create a custom `baseQuery` if your application needs a
different transport or custom request behavior.

------------------------------------------------------------------------

# 34. `builder.query`

``` js
getBooks: builder.query({
  query: () => "/books"
})
```

Read this in plain English:

> "Define a server operation called `getBooks`. It is a query. When
> called without arguments, request `/books`."

So:

``` text
useGetBooksQuery()
       ↓
GET /api/books
```

Queries are primarily for retrieving server data.

Official RTK Query docs describe queries as operations that fetch data
from the server and cache it on the client. [RTK Query
Queries](https://redux-toolkit.js.org/rtk-query/usage/queries)

------------------------------------------------------------------------

# 35. Generated Hooks

When using:

``` js
createApi
```

from:

``` js
@reduxjs/toolkit/query/react
```

RTK Query can generate React hooks.

For:

``` js
getBooks
```

it generates:

``` js
useGetBooksQuery
```

Then:

``` jsx
const {
  data,
  isLoading,
  isFetching,
  error
} = useGetBooksQuery()
```

Do not think this is magic.

Conceptually, the hook is doing something like:

``` text
Component mounts
      ↓
subscribe to getBooks cache entry
      ↓
check cache
      ↓
request if necessary
      ↓
read Redux API state
      ↓
return data/status/error
      ↓
rerender when relevant state changes
```

The actual implementation is more sophisticated, but this is the correct
mental model.

------------------------------------------------------------------------

# 36. What Actually Lives in Redux?

RTK Query adds API state to the Redux store.

Store:

``` js
const store = configureStore({
  reducer: {
    auth: authReducer,

    [booksApi.reducerPath]:
      booksApi.reducer
  },

  middleware: getDefaultMiddleware =>
    getDefaultMiddleware()
      .concat(booksApi.middleware)
})
```

Conceptually:

``` text
Redux Store
│
├── auth
│
├── UI/client state
│
└── booksApi
      │
      ├── queries
      │    └── getBooks(...)
      │
      └── mutations
           └── deleteBook(...)
```

The RTK Query reducer manages API/cache state.

The RTK Query middleware handles much of the coordination around
requests, subscriptions, invalidation, polling, cleanup, and lifecycle
behavior.

RTK Query's internal documentation describes it as being built on Redux
Toolkit's core Redux logic, including slices, reducers, and async
thunks. [RTK Query Internal
Architecture](https://redux-toolkit.js.org/rtk-query/internal/overview)

------------------------------------------------------------------------

# 37. Query Cache Keys

This is one of the most important RTK Query ideas.

Suppose:

``` js
getBook: builder.query({
  query: id => `/books/${id}`
})
```

Then:

``` js
useGetBookQuery(10)
```

and:

``` js
useGetBookQuery(20)
```

are different cache entries.

Conceptually:

``` text
getBook(10) → Book #10
getBook(20) → Book #20
```

The cache is associated with:

``` text
endpoint + arguments
```

So:

``` js
updateQueryData(
  "getBook",
  10,
  ...
)
```

means:

> "Modify the cached result for `getBook(10)`."

------------------------------------------------------------------------

# 38. Multiple Components Can Share One Cache Entry

Suppose:

``` text
Navbar
   ↓
useGetBooksQuery()

BookList
   ↓
useGetBooksQuery()
```

Both ask for:

``` text
getBooks(undefined)
```

RTK Query can use the same cache entry instead of treating them as two
unrelated copies.

Conceptually:

``` text
             getBooks cache
              [A,B,C]
              /     \
             /       \
        Navbar      BookList
```

This is one of the reasons server-state caching is different from simply
putting API responses into a normal Redux slice.

RTK Query also tracks subscriptions and cache lifetime.

------------------------------------------------------------------------

# 39. Subscription and Cache Lifetime

When a component uses:

``` js
useGetBooksQuery()
```

it subscribes to that query's cache entry.

When another component also uses it:

``` text
getBooks cache
   ↑       ↑
   │       │
Navbar   BookList
```

When one unmounts:

``` text
getBooks cache
   ↑
BookList
```

The cache can remain available.

When there are no active subscribers, RTK Query can retain unused data
for a configurable amount of time before removing it.

This avoids immediately throwing away useful cached data every time a
component temporarily disappears.

------------------------------------------------------------------------

# 40. `isLoading` vs `isFetching`

These two are easy to misunderstand.

Imagine the first request:

``` text
No data
  ↓
request
  ↓
loading
```

You can have:

``` js
isLoading === true
```

Now imagine you already have:

``` text
data = [A,B,C]
```

and RTK Query performs a refetch.

During the request:

``` text
data = [A,B,C]
isFetching = true
```

The existing data can remain visible.

This is a huge UX difference.

Instead of:

``` text
[A,B,C]
   ↓
blank screen
   ↓
loading
   ↓
[A,C]
```

you can have:

``` text
[A,B,C]
   ↓
refetching...
   ↓
[A,C]
```

So the user does not experience unnecessary blanking.

------------------------------------------------------------------------

# 41. Mutations

Queries read server data.

Mutations change server data.

Example:

``` js
deleteBook: builder.mutation({
  query: id => ({
    url: `/books/${id}`,
    method: "DELETE"
  })
})
```

Then RTK Query generates:

``` js
const [deleteBook] =
  useDeleteBookMutation()
```

Usage:

``` js
await deleteBook(10)
```

Conceptually:

``` text
Component
   ↓
deleteBook(10)
   ↓
RTK Query mutation
   ↓
DELETE /books/10
   ↓
Backend
   ↓
fulfilled/rejected
```

This resembles `createAsyncThunk` lifecycle behavior.

The difference is that RTK Query connects the mutation to the
**server-state cache lifecycle**.

------------------------------------------------------------------------

# 42. Pessimistic / Server-Confirmed Cache Flow

Start with:

``` text
Cache:

[A, B, C]
```

User deletes B.

A conservative strategy is:

``` text
deleteBook(B)
       ↓
DELETE /books/B
       ↓
wait for server
       ↓
SUCCESS
       ↓
make books cache stale
       ↓
refetch
       ↓
[A,C]
```

If the backend fails:

``` text
deleteBook(B)
       ↓
❌ failure
       ↓
cache remains [A,B,C]
```

There is no rollback because we never changed the cache optimistically.

------------------------------------------------------------------------

# 43. Tags: The Missing Connection

How does RTK Query know that:

``` text
deleteBook
```

affects:

``` text
getBooks
```

?

Tags provide a relationship.

Example:

``` js
tagTypes: ["Books"],

getBooks: builder.query({
  query: () => "/books",
  providesTags: ["Books"]
}),

deleteBook: builder.mutation({
  query: id => ({
    url: `/books/${id}`,
    method: "DELETE"
  }),

  invalidatesTags: ["Books"]
})
```

Think of tags as labels:

``` text
getBooks
   ↓
provides
   ↓
🏷️ Books


deleteBook
   ↓
invalidates
   ↓
🏷️ Books
```

After a successful mutation:

``` text
deleteBook
    ↓
Books tag invalidated
    ↓
getBooks cache considered stale
    ↓
active query can refetch
```

The important idea is not the word "tag".

The idea is:

> **"This query represents this category of server data, and this
> mutation can make that category stale."**

------------------------------------------------------------------------

# 44. Why Invalidation Is Better Than "GET Everything Again"

Without a cache system, you might manually write:

``` text
DELETE
 ↓
GET /books
 ↓
store response
```

With RTK Query:

``` text
mutation
 ↓
invalidate related cache
 ↓
RTK Query determines affected cache entries
 ↓
refetch active subscriptions
```

This is a declarative relationship.

You describe:

``` text
getBooks → provides Books
deleteBook → invalidates Books
```

and RTK Query manages the lifecycle.

------------------------------------------------------------------------

# 45. Optimistic Updates

Now we can finally understand optimistic updates.

The normal approach says:

``` text
Server first
 ↓
then update cache
```

Optimistic says:

> **"I can predict what the cache should look like, so change it
> immediately."**

Suppose:

``` text
[A,B,C]
```

User deletes B.

Immediately:

``` text
[A,C]
```

Then send:

``` text
DELETE /books/B
```

Two things happen in parallel:

``` text
                  deleteBook(B)
                       │
             ┌─────────┴─────────┐
             ↓                   ↓
       update cache          API request
          immediately
             ↓                   ↓
          [A,C]             DELETE /B
```

------------------------------------------------------------------------

# 46. `onQueryStarted`

RTK Query gives lifecycle callbacks.

For a mutation:

``` js
deleteBook: builder.mutation({

  query: id => ({
    url: `/books/${id}`,
    method: "DELETE"
  }),

  async onQueryStarted(
    id,
    { dispatch, queryFulfilled }
  ) {

    // optimistic logic
  }
})
```

Read this as:

> "When this mutation starts, run this custom lifecycle code."

This is similar in spirit to having control inside your custom
middleware.

------------------------------------------------------------------------

# 47. `updateQueryData`

To optimistically change the cache:

``` js
const patchResult = dispatch(
  booksApi.util.updateQueryData(
    "getBooks",
    undefined,
    draft => {

      const index =
        draft.findIndex(book => book.id === id)

      if (index !== -1) {
        draft.splice(index, 1)
      }
    }
  )
)
```

Read the arguments:

``` text
updateQueryData(
    WHICH QUERY,
    WHICH ARGUMENT,
    HOW TO CHANGE ITS CACHE
)
```

So:

``` js
"getBooks"
```

means:

``` text
modify getBooks cache
```

and:

``` js
undefined
```

means:

``` text
getBooks has no query argument
```

and:

``` js
draft => { ... }
```

describes the cache modification.

------------------------------------------------------------------------

# 48. Why Can We `splice()` the Draft?

Because the draft is managed by Immer.

You write:

``` js
draft.splice(index, 1)
```

as if you are mutating an array.

Immer tracks the change and creates the correct immutable state.

So:

``` text
cached data
    ↓
Immer draft
    ↓
draft.splice()
    ↓
new immutable cache state
```

The same fundamental idea you learned with `createSlice` applies here.

------------------------------------------------------------------------

# 49. Why Save `patchResult`?

This:

``` js
const patchResult = dispatch(
  booksApi.util.updateQueryData(...)
)
```

gives you a handle for reversing the patch.

Conceptually:

``` text
Original cache
    ↓
apply patch
    ↓
new cache
    ↓
remember inverse
    ↓
patchResult
```

Then:

``` js
patchResult.undo()
```

means:

> "Reverse the optimistic cache modification."

------------------------------------------------------------------------

# 50. `queryFulfilled`

The mutation gives you:

``` js
queryFulfilled
```

Think of it as:

``` text
Promise:
"Tell me whether this server operation succeeded."
```

So:

``` js
await queryFulfilled
```

means:

``` text
wait for backend result
```

If successful, execution continues.

If rejected, it throws and can be caught.

------------------------------------------------------------------------

# 51. Complete Optimistic Delete

Now everything we've learned comes together:

``` js
deleteBook: builder.mutation({

  query: id => ({
    url: `/books/${id}`,
    method: "DELETE"
  }),

  async onQueryStarted(
    id,
    { dispatch, queryFulfilled }
  ) {

    // 1. Change cache immediately
    const patchResult = dispatch(
      booksApi.util.updateQueryData(
        "getBooks",
        undefined,
        draft => {

          const index =
            draft.findIndex(
              book => book.id === id
            )

          if (index !== -1) {
            draft.splice(index, 1)
          }
        }
      )
    )

    try {

      // 2. Wait for backend
      await queryFulfilled

      // SUCCESS:
      // keep optimistic change

    } catch {

      // FAILURE:
      // restore old cache
      patchResult.undo()
    }
  }
})
```

Do not memorize this.

Read it as a story:

``` text
Mutation starts
      ↓
Change cache NOW
      ↓
Remember how to undo it
      ↓
Send/wait for backend
      ↓
       ┌──────────────┐
       │              │
    success         failure
       │              │
       ↓              ↓
    keep            undo
```

------------------------------------------------------------------------

# 52. Optimistic Failure Example

Before:

``` text
Cache:

[A, B, C]
```

User deletes B.

Immediately:

``` text
[A, C]
```

Backend:

``` text
DELETE /books/B
       ↓
      500
```

Then:

``` text
patchResult.undo()
       ↓
[A,B,C]
```

UI returns to the server-consistent state.

You can additionally show an error:

``` text
"Failed to delete book. Please try again."
```

------------------------------------------------------------------------

# 53. Why Optimistic Updates Are More Complex

Optimistic updates trade:

``` text
better perceived speed
```

for:

``` text
more correctness logic
```

You have to think about:

``` text
What did I change?
Can I calculate the correct new value?
How do I undo it?
What if another request changes the same data?
What if the server applies different business rules?
```

So use optimism when the UX benefit is worth the complexity.

Examples where optimism is common:

``` text
Like a post
Toggle favorite
Delete a UI item
Add/remove a simple item
```

For critical operations, be more conservative.

------------------------------------------------------------------------

# 54. Pessimistic vs Optimistic

### Server-confirmed / conservative

``` text
User action
    ↓
request
    ↓
backend
    ↓
success
    ↓
cache update/refetch
```

Pros:

``` text
simple
safe
server is authoritative
```

Cons:

``` text
user may wait
```

### Optimistic

``` text
User action
    ↓
cache update immediately
    ↓
request
    ↓
success → keep
failure → rollback
```

Pros:

``` text
instant UX
```

Cons:

``` text
rollback logic
more edge cases
```

------------------------------------------------------------------------

# 55. The Full Evolution --- One Continuous Story

This is the most important section.

## Stage 1 --- Component state

``` text
Component
   ↓
useState
   ↓
local UI
```

Problem:

> Other distant components need the same state.

------------------------------------------------------------------------

## Stage 2 --- Props

``` text
Parent
  ↓ props
Child
  ↓ props
Grandchild
```

Problem:

> Prop drilling.

------------------------------------------------------------------------

## Stage 3 --- Context

``` text
Provider
   ↓
React subtree
   ↓
useContext()
```

Problem solved:

> Distribution of shared values.

But Context isn't a full state-management architecture.

------------------------------------------------------------------------

## Stage 4 --- Redux

``` text
Component
   ↓
dispatch(action)
   ↓
Store
   ↓
Reducer
   ↓
new state
   ↓
Component
```

Problem solved:

> Structured global state management.

------------------------------------------------------------------------

## Stage 5 --- Redux Toolkit

``` text
createSlice
configureStore
Immer
generated action creators
```

Problem solved:

> Redux was powerful but repetitive.

------------------------------------------------------------------------

## Stage 6 --- API calls

You try:

``` text
Component
   ↓
Service
   ↓
Backend
   ↓
dispatch(response)
   ↓
Redux
```

Problem:

> Async/API orchestration becomes mixed with UI and repeated lifecycle
> code.

------------------------------------------------------------------------

## Stage 7 --- Middleware

``` text
dispatch
   ↓
middleware
   ↓
API / side effect
   ↓
dispatch result
   ↓
reducer
```

Problem solved:

> Side effects need a place outside reducers.

------------------------------------------------------------------------

## Stage 8 --- Thunk

``` text
dispatch(function)
       ↓
thunk middleware
       ↓
execute function
       ↓
API
       ↓
dispatch actions
```

Problem solved:

> Async workflows need access to dispatch/getState.

------------------------------------------------------------------------

## Stage 9 --- `createAsyncThunk`

``` text
createAsyncThunk
      ↓
pending
      ↓
API
      ↓
fulfilled / rejected
```

Problem solved:

> Repetitive async lifecycle state: loading/data/error.

------------------------------------------------------------------------

## Stage 10 --- RTK Query

Now the problem changes.

``` text
Server state
```

is not simply:

``` text
Redux state + fetch()
```

It has its own lifecycle:

``` text
cache
subscriptions
staleness
refetching
deduplication
invalidation
cleanup
mutation coordination
optimistic updates
```

RTK Query is purpose-built for this.

------------------------------------------------------------------------

# 56. What RTK Query Is Actually Doing Underneath

A useful conceptual model is:

``` text
                     RTK Query
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
      Redux          Middleware         Hooks
        │                │                │
        ▼                ▼                ▼
   API/cache state   lifecycle logic   React bridge
        │
        ▼
   Query cache
        │
        ▼
   server data
```

The official internal architecture describes RTK Query as built on Redux
Toolkit's Redux logic, including slices, reducers, and async thunks.
[RTK Query Internal
Architecture](https://redux-toolkit.js.org/rtk-query/internal/overview)

So RTK Query is not replacing Redux.

It is using Redux Toolkit to build a specialized **server-state layer**.

------------------------------------------------------------------------

# 57. Query Lifecycle --- What Happens When a Component Mounts?

Suppose:

``` jsx
const { data, isLoading } =
  useGetBooksQuery()
```

Conceptually:

``` text
Component renders
       ↓
useGetBooksQuery()
       ↓
identify cache entry:
getBooks(undefined)
       ↓
Does appropriate cache exist?
       │
       ├── yes → use existing cache
       │
       └── no → initiate request
                    ↓
                 baseQuery
                    ↓
                 backend
                    ↓
                 response
                    ↓
              Redux API state
                    ↓
              component updates
```

This is why you usually don't need:

``` js
useEffect(() => {
  dispatch(fetchBooks())
}, [])
```

for a standard RTK Query read.

The hook itself represents the component's subscription to the resource.

------------------------------------------------------------------------

# 58. Second Component

Suppose another component calls:

``` js
useGetBooksQuery()
```

It asks for the same cache entry:

``` text
getBooks(undefined)
```

Conceptually:

``` text
                 getBooks cache
                    [A,B,C]
                    /     \
                   /       \
             Component A  Component B
```

They can share the same server-state cache.

This is fundamentally different from each component independently
fetching and storing its own copy.

------------------------------------------------------------------------

# 59. Refetching Without Blank Screens

Suppose cache currently contains:

``` text
[A,B,C]
```

A refetch starts.

RTK Query can represent:

``` text
data = [A,B,C]
isFetching = true
```

So UI can remain:

``` text
A
B
C

Updating...
```

instead of:

``` text
Loading...
```

with no data.

This is one reason the distinction between:

``` text
initial loading
```

and:

``` text
background fetching/refetching
```

matters.

------------------------------------------------------------------------

# 60. Avoiding Duplicate Requests

Imagine two components need the same resource at nearly the same time.

Without a server-state cache:

``` text
Component A → GET /books
Component B → GET /books
```

Potentially two requests.

RTK Query can identify the same endpoint + arguments as the same cache
entry and coordinate the request/cache lifecycle.

The official query documentation explicitly discusses cache keys and
avoiding unnecessary duplicate requests. [RTK Query
Queries](https://redux-toolkit.js.org/rtk-query/usage/queries)

------------------------------------------------------------------------

# 61. RTK Query vs `createAsyncThunk`

This distinction should become automatic.

### `createAsyncThunk`

Think:

> "I need to perform an async workflow."

Example:

``` text
login
upload file
send analytics event
run custom async business workflow
```

You define the Redux state that should respond to:

``` text
pending
fulfilled
rejected
```

### RTK Query

Think:

> "This component/app needs a server resource, and I want its
> fetching/cache lifecycle managed."

Example:

``` text
books
borrowed books
users
courses
notifications
```

RTK Query handles:

``` text
request
cache
subscription
refetch
invalidation
lifecycle
```

------------------------------------------------------------------------

# 62. What Should Go Where?

A practical architecture:

``` text
                    APPLICATION STATE
                           │
          ┌────────────────┴────────────────┐
          │                                 │
          ▼                                 ▼
     CLIENT STATE                       SERVER STATE
          │                                 │
          ▼                                 ▼
 Redux slices                         RTK Query
 / local React state                  cache
          │                                 │
          │                                 │
   ┌──────┼───────┐                 ┌───────┼───────┐
   ▼      ▼       ▼                 ▼       ▼       ▼
 theme   modal   selected          books   users   borrows
         tab     ID
```

Examples:

### Local React state

``` text
modalOpen
inputValue
hovered
selectedTab
```

### Redux slice

``` text
global UI preferences
authentication/client session metadata
workflow state
complex client-only state
```

### RTK Query

``` text
books from backend
users from backend
borrowed books
notifications from backend
```

Do not automatically put every piece of state into Redux.

------------------------------------------------------------------------

# 63. Avoid This RTK Query Anti-Pattern

If RTK Query already owns:

``` text
booksApi → getBooks → data
```

don't immediately copy it into:

``` text
booksSlice → books
```

just because you are used to Redux.

That creates:

``` text
server cache
    ↓
copy
    ↓
client Redux state
```

Now you have two sources of truth.

Prefer:

``` text
RTK Query cache
      ↓
component
```

unless you have a real reason to transform/copy data into separate
client state.

------------------------------------------------------------------------

# 64. Recommended Mental Model for Your Library Project

Imagine:

``` text
Library Frontend
│
├── Auth
│    └── client/auth state
│
├── UI
│    ├── modal
│    ├── selected tab
│    └── filters
│
└── booksApi
     │
     ├── getBooks
     ├── getBook
     ├── addBook
     ├── updateBook
     ├── deleteBook
     ├── getBorrowedBooks
     └── returnBook
```

Then:

``` text
Admin
   ↓
booksApi
   ↓
backend

Librarian
   ↓
booksApi
   ↓
backend

User
   ↓
booksApi
   ↓
backend
```

The different UIs do not need completely separate fetching
architectures.

They can subscribe to the server resources they need.

------------------------------------------------------------------------

# 65. A Complete Minimal RTK Query Example

``` js
import {
  createApi,
  fetchBaseQuery
} from "@reduxjs/toolkit/query/react"

export const booksApi = createApi({

  reducerPath: "booksApi",

  baseQuery: fetchBaseQuery({
    baseUrl: "http://localhost:8080/api"
  }),

  tagTypes: ["Books"],

  endpoints: builder => ({

    // READ
    getBooks: builder.query({
      query: () => "/books",
      providesTags: ["Books"]
    }),

    // CREATE
    addBook: builder.mutation({
      query: book => ({
        url: "/books",
        method: "POST",
        body: book
      }),
      invalidatesTags: ["Books"]
    }),

    // UPDATE
    updateBook: builder.mutation({
      query: ({ id, ...changes }) => ({
        url: `/books/${id}`,
        method: "PUT",
        body: changes
      }),
      invalidatesTags: ["Books"]
    }),

    // DELETE
    deleteBook: builder.mutation({
      query: id => ({
        url: `/books/${id}`,
        method: "DELETE"
      }),
      invalidatesTags: ["Books"]
    })
  })
})

export const {
  useGetBooksQuery,
  useAddBookMutation,
  useUpdateBookMutation,
  useDeleteBookMutation
} = booksApi
```

This is already enough for a large amount of ordinary CRUD development.

------------------------------------------------------------------------

# 66. Store Configuration

``` js
import { configureStore } from "@reduxjs/toolkit"
import authReducer from "../features/auth/authSlice"
import { booksApi } from "../services/booksApi"

export const store = configureStore({

  reducer: {

    auth: authReducer,

    [booksApi.reducerPath]:
      booksApi.reducer
  },

  middleware: getDefaultMiddleware =>
    getDefaultMiddleware()
      .concat(booksApi.middleware)
})
```

Why two pieces?

``` text
booksApi.reducer
      ↓
stores API/cache state

booksApi.middleware
      ↓
runs RTK Query lifecycle machinery
```

You need both.

------------------------------------------------------------------------

# 67. Component

``` jsx
function BooksPage() {

  const {
    data: books = [],
    isLoading,
    isFetching,
    error
  } = useGetBooksQuery()

  const [deleteBook] =
    useDeleteBookMutation()

  if (isLoading) {
    return <p>Loading books...</p>
  }

  if (error) {
    return <p>Failed to load books</p>
  }

  return (
    <>
      {isFetching && <p>Updating...</p>}

      {books.map(book => (
        <div key={book.id}>
          {book.title}

          <button
            onClick={() => deleteBook(book.id)}
          >
            Delete
          </button>
        </div>
      ))}
    </>
  )
}
```

Notice how little API lifecycle code is in the component.

The component mainly describes:

> "I need books."

That is the architectural payoff.

------------------------------------------------------------------------

# 68. The Ultimate Mental Model

You can compress the entire learning journey into these questions.

### Question 1

> Is this state local to one component?

``` text
Yes → useState
```

### Question 2

> Is it shared across a React subtree and mainly needs distribution?

``` text
Context may be enough
```

### Question 3

> Is it complex shared client state with structured transitions?

``` text
Redux Toolkit slice
```

### Question 4

> Does changing it require an async workflow?

``` text
Thunk / createAsyncThunk
```

### Question 5

> Is the state actually data owned by a backend?

``` text
RTK Query
```

### Question 6

> Does a mutation make cached server data stale?

``` text
Tags / invalidation
```

### Question 7

> Can I predict the immediate cache change and want instant UX?

``` text
Optimistic update
```

### Question 8

> What if my optimistic assumption is wrong?

``` text
rollback / undo
```

------------------------------------------------------------------------

# 69. Final Architecture

For a modern React + Redux Toolkit application:

``` text
                         React
                           │
            ┌──────────────┴──────────────┐
            │                             │
       local UI state                shared state
            │                             │
        useState                    Redux Toolkit
                                          │
                           ┌──────────────┴──────────────┐
                           │                             │
                     client state                 server state
                           │                             │
                       createSlice                   RTK Query
                           │                             │
                           │                  ┌──────────┴──────────┐
                           │                  │                     │
                           │                query               mutation
                           │                  │                     │
                           │               cache             invalidate/update
                           │                  │                     │
                           │                  └──────────┬──────────┘
                           │                             │
                           └──────────────┬──────────────┘
                                          │
                                    Redux Store
                                          │
                                    React-Redux
                                          │
                                      Components
```

And the data flow:

``` text
                    USER INTERACTION
                           │
                           ▼
                       Component
                           │
             ┌─────────────┴─────────────┐
             │                           │
        client action                server action
             │                           │
             ▼                           ▼
       Redux dispatch              RTK Query hook
             │                           │
             ▼                           ▼
        slice/reducer              API/cache system
             │                           │
             ▼                           ▼
       Redux client state           Backend server
             │                           │
             └─────────────┬─────────────┘
                           ▼
                         UI
```

------------------------------------------------------------------------

# 70. The "No Magic" Version

Whenever you see:

``` js
useGetBooksQuery()
```

mentally expand it to:

``` text
"I want this component to subscribe to
the getBooks server resource."

        ↓

"Find its cache entry."

        ↓

"If necessary, request it."

        ↓

"Keep the result in Redux-managed API state."

        ↓

"Tell me when it is loading/fetching/error."

        ↓

"Keep this subscription alive while I need it."

        ↓

"Coordinate cache/refetch lifecycle."
```

Whenever you see:

``` js
dispatch(fetchBooks())
```

think:

``` text
"This is a thunk workflow."

        ↓

"Run async logic."

        ↓

"Dispatch lifecycle actions."

        ↓

"Reducers update client state."
```

Whenever you see:

``` js
createSlice(...)
```

think:

``` text
"Define one area of client state."

        ↓

"Generate reducer."

        ↓

"Generate action creators."

        ↓

"Use Immer for convenient immutable updates."
```

Whenever you see:

``` js
builder.mutation(...)
```

think:

``` text
"This server operation changes backend state."

        ↓

"RTK Query must coordinate
the resulting cache consequences."
```

Whenever you see:

``` js
updateQueryData(...)
```

think:

``` text
"I am manually telling the server-state cache
what the next value should look like."
```

Whenever you see:

``` js
patchResult.undo()
```

think:

``` text
"My optimistic prediction was wrong.
Restore the previous cache."
```

------------------------------------------------------------------------

# 71. What You Actually Need to Memorize

Very little.

You should **understand** the architecture and memorize only the small
vocabulary you repeatedly use:

``` text
Redux
  → store
  → action
  → reducer
  → dispatch
  → selector

Redux Toolkit
  → configureStore
  → createSlice
  → createAsyncThunk

RTK Query
  → createApi
  → baseQuery
  → builder.query
  → builder.mutation
  → generated hooks
  → providesTags
  → invalidatesTags
  → updateQueryData
  → queryFulfilled
  → patchResult.undo
```

The APIs are documentation-searchable.

The architecture should live in your head.

------------------------------------------------------------------------

# 72. One-Sentence Definitions

**Context**

> A React mechanism for making a value available to descendants without
> passing it through props.

**Redux**

> A predictable architecture for centralized state transitions using
> actions and reducers.

**Redux Toolkit**

> The officially recommended toolbox for writing Redux logic with less
> boilerplate and safer defaults.

**Slice**

> A self-contained section of Redux state plus the reducers/actions that
> operate on it.

**Middleware**

> A pipeline between dispatch and the reducer where cross-cutting logic
> and side effects can run.

**Thunk**

> A middleware pattern that lets dispatch accept a function so
> asynchronous workflows can execute and later dispatch normal actions.

**`createAsyncThunk`**

> A standardized RTK abstraction for async workflows with
> pending/fulfilled/rejected lifecycle actions.

**RTK Query**

> A Redux Toolkit addon specialized for fetching, caching, subscribing
> to, and synchronizing server state.

**Optimistic update**

> Change the client cache before server confirmation because you predict
> the server operation will succeed.

**Rollback**

> Reverse that optimistic cache change when the server rejects the
> operation.

------------------------------------------------------------------------

# 73. Final Mental Picture

If you remember only one diagram, remember this:

``` text
                       YOUR REACT APP
                              │
               ┌──────────────┴──────────────┐
               │                             │
               ▼                             ▼
          CLIENT STATE                 SERVER STATE
               │                             │
               ▼                             ▼
         Redux Toolkit                    RTK Query
               │                             │
        ┌──────┴──────┐             ┌───────┴────────┐
        │             │             │                │
   createSlice   createAsyncThunk  query          mutation
        │             │             │                │
        │             │             │          ┌─────┴─────┐
        │             │             │          │           │
        │             │             │       pessimistic  optimistic
        │             │             │          │           │
        │             │             │       invalidate   update cache
        │             │             │          │           │
        │             │             │       refetch      rollback
        │             │             │
        └─────────────┴─────────────┴──────────────┐
                                                   │
                                             Redux Store
                                                   │
                                             React-Redux
                                                   │
                                               Components
```

The whole progression is therefore not a collection of unrelated APIs.

It is one story:

``` text
LOCAL STATE
    ↓
sharing problem
    ↓
PROPS / PROP DRILLING
    ↓
distribution problem
    ↓
CONTEXT
    ↓
structured shared-state problem
    ↓
REDUX
    ↓
boilerplate problem
    ↓
REDUX TOOLKIT
    ↓
side-effect problem
    ↓
MIDDLEWARE
    ↓
async-function problem
    ↓
THUNK
    ↓
async lifecycle boilerplate
    ↓
CREATE ASYNC THUNK
    ↓
server-state lifecycle problem
    ↓
RTK QUERY
    ↓
cache synchronization
    ↓
INVALIDATION / REFETCH
    ↓
instant UX
    ↓
OPTIMISTIC UPDATE
    ↓
server disagrees
    ↓
ROLLBACK
```

That is the architecture.

The syntax is simply the vocabulary we use to tell the machinery which
part of that story we want.
