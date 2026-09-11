import api from "../app/axios/Axios.js";

const cleanParams = (params) => Object.fromEntries(
  Object.entries(params).filter(([, value]) => value !== "" && value !== null && value !== undefined),
);

export const getBooks = (params = {}) => api.get("/books", { params: cleanParams(params) });
export const createBook = (book) => api.post("/books", book);
export const updateBook = (id, book) => api.put(`/books/${id}`, book);
export const deleteBook = (id) => api.delete(`/books/${id}`);
export const borrowBook = (id) => api.post(`/borrow/${id}`);
export const getBorrows = (params = {}) => api.get("/borrow", { params: cleanParams(params) });
export const getMyBorrows = (params = {}) => api.get("/borrow/my", { params: cleanParams(params) });
export const approveBorrow = (id) => api.post(`/borrow/${id}/approve`);
export const returnBook = (id) => api.post(`/borrow/${id}/return`);
export const getUsers = () => api.post("/users");
export const createLibrarian = (user) => api.post("/users/librarian", user);
