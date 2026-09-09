import React from 'react'

const BorrowedBooks = ({borrowedBooks}) => {
  return (
    <div>
      <h2>Borrowed Books</h2>
     
        {borrowedBooks.map((book) => (
         <div key={book.borrowId}
   class="bg-white border border-slate-200 shadow-sm w-full max-w-sm rounded-lg mx-auto mt-6 p-4 sm:p-6 dark:bg-neutral-800 dark:border-neutral-700">
   <div>
      <h3 class="text-slate-900 text-base font-semibold dark:text-slate-50">{book.borrowedBook.authorName}</h3>
      <p class="mt-2 text-sm text-slate-600 leading-relaxed dark:text-slate-400">{book.borrowedBook.name}</p>
   </div>
  <div className="flex gap-4 mt-4">
   <p href="#"
      class="inline-block mt-6 py-2 px-3.5 text-sm rounded-md font-semibold cursor-pointer text-white border border-blue-600 bg-blue-600 hover:bg-blue-700 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500">
    Due: {book.dueDate}
    
   </p>
    <p href="#"
      class="inline-block mt-6 py-2 px-3.5 text-sm rounded-md font-semibold cursor-pointer text-white border border-blue-600 bg-blue-600 hover:bg-blue-700 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500">
     Borrowed at: {book.borrowedAt}
    
   </p>
   </div>
  

</div>
        ))}
     
    </div>
  )
}

export default BorrowedBooks