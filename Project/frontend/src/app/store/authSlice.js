import {createSlice} from "@reduxjs/toolkit";

const token = localStorage.getItem("token");


const initialState = {
  isAuthenticated: !!token,
  token: token,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
    reducers: {
        login:(state, action) => {
            state.isAuthenticated = true;
            state.token = action.payload?.token;
        },
        logout:(state) => {
            state.isAuthenticated = false;
            state.token = null;
        }
    }
});
export const {login, logout} = authSlice.actions;
export default authSlice.reducer;