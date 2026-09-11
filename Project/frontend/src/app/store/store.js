import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./authSlice";
import borrowReducer from "./borrowedBookSlice";
import apiMiddleware from "../../middlewares/apiMiddleWare";
import {booksApi} from "../../service/BooksApi"
const store = configureStore({
  reducer: { auth: authReducer, borrows: borrowReducer,[booksApi.reducerPath] : booksApi.reducer},
  devTools: true,
  middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(apiMiddleware).concat(booksApi.middleware),
});

export default store;  

