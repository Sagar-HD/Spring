import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { selectUserData } from "../app/store/authSlice";
import { borrowBook, createBook, deleteBook, getBooks, updateBook } from "../service/LibraryService";

const blank = { name: "", authorName: "", noOfCopies: 1 };

export default function Books({ manage = false }) {
  const user = useSelector(selectUserData);
  const canManage = manage || user?.roles?.some((role) => /ADMIN|LIBRARIAN/i.test(role));
  const [books, setBooks] = useState([]);
  const [filters, setFilters] = useState({ title: "", authorName: "" });
  const [form, setForm] = useState(blank);
  const [editing, setEditing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  async function loadBooks() {
    setLoading(true);
    try { setBooks((await getBooks(filters)).data || []); }
    catch (e) { setError(e.response?.data?.message || "Unable to load books."); }
    finally { setLoading(false); }
  }
  useEffect(() => { loadBooks(); }, []);

  async function save(event) {
    event.preventDefault(); setError("");
    try {
      if (editing) await updateBook(editing, form); else await createBook(form);
      setNotice(editing ? "Book updated." : "Book added."); setForm(blank); setEditing(null); await loadBooks();
    } catch (e) { setError(e.response?.data?.message || "Could not save the book."); }
  }
  async function run(action, success) {
    setError("");
    try { await action(); setNotice(success); await loadBooks(); }
    catch (e) { setError(e.response?.data?.message || "The action could not be completed."); }
  }

  return <section className="mx-auto max-w-7xl space-y-7">
    <header className="rounded-3xl bg-slate-900 px-6 py-8 text-white shadow-xl sm:px-10">
      <p className="text-sm font-semibold uppercase tracking-[0.2em] text-blue-300">Collection</p>
      <h1 className="mt-2 text-3xl font-bold sm:text-4xl">{canManage ? "Manage books" : "Find your next read"}</h1>
      <p className="mt-3 text-slate-300">Browse the catalogue and keep every loan moving.</p>
    </header>
    {(notice || error) && <p className={`rounded-xl px-4 py-3 text-sm ${error ? "bg-red-50 text-red-700" : "bg-emerald-50 text-emerald-700"}`}>{error || notice}</p>}
    {canManage && <form onSubmit={save} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="mb-4 flex justify-between"><h2 className="text-lg font-bold">{editing ? "Edit book" : "Add a book"}</h2>{editing && <button type="button" onClick={() => { setEditing(null); setForm(blank); }} className="text-sm text-slate-500">Cancel</button>}</div>
      <div className="grid gap-3 sm:grid-cols-3">{[["name", "Book title", "text"], ["authorName", "Author", "text"], ["noOfCopies", "Copies", "number"]].map(([key, label, type]) => <label key={key} className="text-sm font-medium text-slate-700">{label}<input required type={type} min={type === "number" ? 1 : undefined} value={form[key]} onChange={(e) => setForm({ ...form, [key]: type === "number" ? Number(e.target.value) : e.target.value })} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2" /></label>)}</div>
      <button className="mt-4 rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white">{editing ? "Save changes" : "Add book"}</button>
    </form>}
    <div className="flex flex-col gap-3 sm:flex-row"><input value={filters.title} onChange={(e) => setFilters({ ...filters, title: e.target.value })} placeholder="Search by title" className="flex-1 rounded-lg border border-slate-300 px-4 py-2.5" /><input value={filters.authorName} onChange={(e) => setFilters({ ...filters, authorName: e.target.value })} placeholder="Search by author" className="flex-1 rounded-lg border border-slate-300 px-4 py-2.5" /><button onClick={loadBooks} className="rounded-lg bg-slate-800 px-5 py-2.5 font-semibold text-white">Search</button></div>
    {loading ? <p className="text-slate-500">Loading catalogue...</p> : books.length === 0 ? <div className="rounded-2xl border border-dashed border-slate-300 p-12 text-center text-slate-500">No books match your search.</div> : <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">{books.map((book) => <article key={book.id} className="flex flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"><div className="mb-5 text-3xl">📚</div><h2 className="text-lg font-bold">{book.name || book.title}</h2><p className="text-sm text-slate-500">by {book.authorName}</p><p className="mt-4 text-sm font-medium">{book.availableCopies ?? 0} of {book.noOfCopies ?? 0} copies available</p><div className="mt-auto flex gap-2 pt-5">{!canManage && <button disabled={!book.availableCopies} onClick={() => run(() => borrowBook(book.id), "Borrow request submitted.")} className="rounded-lg bg-blue-600 px-3 py-2 text-sm font-semibold text-white disabled:bg-slate-300">Borrow</button>}{canManage && <><button onClick={() => { setEditing(book.id); setForm({ name: book.name || book.title || "", authorName: book.authorName || "", noOfCopies: book.noOfCopies || 1 }); }} className="rounded-lg border px-3 py-2 text-sm font-semibold">Edit</button><button onClick={() => run(() => deleteBook(book.id), "Book deleted.")} className="rounded-lg border border-red-200 px-3 py-2 text-sm font-semibold text-red-600">Delete</button></>}</div></article>)}</div>}
  </section>;
}
