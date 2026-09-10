import { useState } from 'react'
import heroImg from './assets/hero.png'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import './App.css'
import {useSelector} from 'react-redux'
import {getUserData} from './service/UserService'
import {getMyBorrowedBooks} from './service/BookServices'
import { useEffect } from 'react'
import BorrowedBooks from './pages/BorrowedBooks'
import { useDispatch } from 'react-redux'
import { selectBorrowedBooks,selectBorrowedBooksError,selectBorrowedBooksLoading } from './app/store/borrowedBookSlice'

function App() {
 const auth = useSelector((state) => state.auth);
 const dispatch = useDispatch();
 const borrowedBooks = useSelector(selectBorrowedBooks);
 const borrowedBooksLoading = useSelector(selectBorrowedBooksLoading);
 const borrowedBooksError = useSelector(selectBorrowedBooksError);



 


 useEffect(() => {

  dispatch({ type: 'api/makeRequest', payload: { url:"/borrow/my", onSuccess: 'borrows/onBorrowedBooksSuccess', onError: 'borrows/onBorrowedBooksFailure' } });
   console.log("borrowedBooks",borrowedBooks);


 },[]);


  return (
    <>
    <h1>app  hello 
      


    </h1>
   {
    borrowedBooksLoading ? (
      <p>Loading borrowed books...</p>
    ) : borrowedBooksError ? (
      <p>Error: {borrowedBooksError}</p>
    ) : (
      <BorrowedBooks borrowedBooks={borrowedBooks} />
    )
   }
    </>
  )
}

export default App
