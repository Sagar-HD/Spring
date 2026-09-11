import { createSlice } from '@reduxjs/toolkit';
const initialState = {
    borrowedBooks: [],
    loading: false,
    error: null,
};
const borrowSlice = createSlice({
    name: 'borrows',
    initialState,
    reducers: {
        onBorrowedBooksStart: (state) => {
            state.loading = true;
            state.error = null;
        }
          ,
        onBorrowedBooksSuccess: (state, action) => {
       
            state.loading = false;
           
            state.borrowedBooks = action.payload;
        },
        onBorrowedBooksFailure: (state, action) => {
            state.loading = false;
            state.error = action.payload;
        },


    },
});

export const selectBorrowedBooks = (state) => state.borrows.borrowedBooks;
export const selectBorrowedBooksLoading = (state) => state.borrows.loading;
export const selectBorrowedBooksError = (state) => state.borrows.error;
export const { onBorrowedBooksStart, onBorrowedBooksSuccess, onBorrowedBooksFailure } = borrowSlice.actions;
export default borrowSlice.reducer;