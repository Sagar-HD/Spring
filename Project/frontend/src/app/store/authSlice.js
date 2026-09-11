import {createSlice} from "@reduxjs/toolkit";



const initialState = {
  status: "checking",
  userData:null
  
};

const authSlice = createSlice({
  name: "auth",
  initialState,
    reducers: {
        login:(state, action) => {
           state.status = "authenticated";
            state.userData = action.payload;
        },
         authenticatedCheckFailed: (state) => {
      state.status = "unauthenticated";
      state.userData = null;
    },


        logout:(state) => {
             state.status = "unauthenticated";
              state.userData = null;
        }
    }
});

export const selectUserData = (state) => state.auth.userData;
export const selectAuthStatus = (state) => state.auth.status;

export const {login, logout, authenticatedCheckFailed} = authSlice.actions;
export default authSlice.reducer;