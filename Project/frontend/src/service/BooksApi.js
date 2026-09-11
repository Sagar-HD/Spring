import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export const booksApi = createApi({
  reducerPath: "booksApi",
  baseQuery: fetchBaseQuery({
    baseUrl: import.meta.env.VITE_BACKEND_URL,
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("token");
      if (token) headers.set("Authorization", "Bearer " + token);
      headers.set("Content-Type", "application/json");
      return headers;
    },
  }),
  tagTypes: ["Book", "Borrow", "User"],
  endpoints: (builder) => ({
    getAllBooks: builder.query({
      query: (params = {}) => ({ url: "/books", params: cleanParams(params) }),
      providesTags: ["Book"],
    }),
    addBook: builder.mutation({ query: (body) => ({ url: "/books", method: "POST", body }), invalidatesTags: ["Book"] }),
    updateBook: builder.mutation({ query: ({ id, ...body }) => ({ url: `/books/${id}`, method: "PUT", body }), invalidatesTags: ["Book"] }),
    deleteBook: builder.mutation({ query: (id) => ({ url: `/books/${id}`, method: "DELETE" }), invalidatesTags: ["Book"] }),
    borrowBook: builder.mutation({ query: (id) => ({ url: `/borrow/${id}`, method: "POST" }), invalidatesTags: ["Borrow", "Book"] }),
    getMyBorrows: builder.query({ query: (params = {}) => ({ url: "/borrow/my", params: cleanParams(params) }), providesTags: ["Borrow"] }),
    getAllBorrows: builder.query({ query: (params = {}) => ({ url: "/borrow", params: cleanParams(params) }), providesTags: ["Borrow"] }),
    approveBorrow: builder.mutation({ query: (id) => ({ url: `/borrow/${id}/approve`, method: "POST" }), invalidatesTags: ["Borrow", "Book"] }),
    returnBook: builder.mutation({ query: (id) => ({ url: `/borrow/${id}/return`, method: "POST" }), invalidatesTags: ["Borrow", "Book"] }),
    getUsers: builder.query({ query: () => ({ url: "/users", method: "POST" }), providesTags: ["User"] }),
    addLibrarian: builder.mutation({ query: (body) => ({ url: "/users/librarian", method: "POST", body }), invalidatesTags: ["User"] }),
  }),
});

function cleanParams(params) {
  return Object.fromEntries(Object.entries(params).filter(([, value]) => value !== "" && value !== null && value !== undefined));
}

export const {
  useGetAllBooksQuery, useAddBookMutation, useUpdateBookMutation, useDeleteBookMutation,
  useBorrowBookMutation, useGetMyBorrowsQuery, useGetAllBorrowsQuery,
  useApproveBorrowMutation, useReturnBookMutation, useGetUsersQuery, useAddLibrarianMutation,
} = booksApi;
