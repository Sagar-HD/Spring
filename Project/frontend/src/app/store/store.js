import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./authSlice";
import borrowReducer from "./borrowedBookSlice";
import apiMiddleware from "../../middlewares/apiMiddleWare";
const store = configureStore({
  reducer: { auth: authReducer, borrows: borrowReducer, },
  devTools: true,
  middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(apiMiddleware),
});

export default store;  

