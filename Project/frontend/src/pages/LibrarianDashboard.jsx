import { Link } from "react-router-dom";
import { useGetAllBooksQuery, useGetAllBorrowsQuery } from "../service/BooksApi";

export default function LibrarianDashboard() {
  const { data: books = [], isLoading: booksLoading } = useGetAllBooksQuery();
  const { data: borrows = [], isLoading: borrowsLoading } = useGetAllBorrowsQuery();
  const pending = borrows.filter((item) => String(item.borrowStatus || "").toUpperCase() === "REQUESTED").length;
  return (
    <section className="mx-auto max-w-7xl space-y-8">
      <header className="rounded-3xl bg-gradient-to-br from-emerald-700 to-teal-900 p-8 text-white shadow-xl">
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-emerald-200">Librarian workspace</p>
        <h1 className="mt-3 text-4xl font-bold">Keep the library moving.</h1>
        <p className="mt-3 max-w-xl text-emerald-100">Manage the catalogue and process member borrowing requests from one place.</p>
      </header>
      <div className="grid gap-5 md:grid-cols-3">
        {[["Catalogue", booksLoading ? "..." : books.length, "/librarian/books", "📚"], ["Borrow records", borrowsLoading ? "..." : borrows.length, "/librarian/borrows", "↗"], ["Pending requests", pending, "/librarian/borrows", "⏱"]].map(([label, value, to, icon]) => <Link key={label} to={to} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg"><span className="text-2xl">{icon}</span><p className="mt-5 text-sm font-medium text-slate-500">{label}</p><p className="mt-1 text-3xl font-bold text-slate-900">{value}</p><span className="mt-4 inline-block text-sm font-semibold text-emerald-700">Open →</span></Link>)}
      </div>
    </section>
  );
}
