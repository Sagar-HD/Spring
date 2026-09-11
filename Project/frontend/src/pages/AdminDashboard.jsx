import { Link } from "react-router-dom";
import { useGetAllBooksQuery, useGetAllBorrowsQuery, useGetUsersQuery } from "../service/BooksApi";

export default function AdminDashboard() {
  const { data: books = [], isLoading: booksLoading } = useGetAllBooksQuery();
  const { data: borrows = [], isLoading: borrowsLoading } = useGetAllBorrowsQuery();
  const { data: users = [], isLoading: usersLoading } = useGetUsersQuery();
  const cards = [["Books", booksLoading ? "..." : books.length, "/admin/books", "📚"], ["Borrow records", borrowsLoading ? "..." : borrows.length, "/admin/borrows", "↗"], ["Registered users", usersLoading ? "..." : users.length, "/admin/users", "👥"]];
  return (
    <section className="mx-auto max-w-7xl space-y-8">
      <header className="rounded-3xl bg-gradient-to-br from-indigo-800 to-slate-950 p-8 text-white shadow-xl">
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-indigo-200">Administration</p>
        <h1 className="mt-3 text-4xl font-bold">Library overview.</h1>
        <p className="mt-3 max-w-xl text-slate-300">Monitor your collection, circulation activity, and users.</p>
      </header>
      <div className="grid gap-5 md:grid-cols-3">{cards.map(([label, value, to, icon]) => <Link key={label} to={to} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg"><span className="text-2xl">{icon}</span><p className="mt-5 text-sm font-medium text-slate-500">{label}</p><p className="mt-1 text-3xl font-bold text-slate-900">{value}</p><span className="mt-4 inline-block text-sm font-semibold text-indigo-700">Manage →</span></Link>)}</div>
    </section>
  );
}
