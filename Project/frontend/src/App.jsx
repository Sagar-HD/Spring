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
function App() {
 const auth = useSelector((state) => state.auth);
 const [userData, setUserData] = useState({});
 const [borrowedBooks, setBorrowedBooks] = useState([]);


 useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await getUserData();
        setUserData(response.data);
        const borrowedBooksResponse = await getMyBorrowedBooks();
        setBorrowedBooks(borrowedBooksResponse.data);
      } catch (error) {
        console.error("Error fetching user data:", error);
      }
    };
    
    fetchUserData();
  
 }, [auth]);


  return (
    <>
    <h1>app  hello 
      {
        userData && userData ? `Welcome, ${userData.name}! email : ${userData.email}`  : 'Loading user data...'
      }


    </h1>
    <BorrowedBooks borrowedBooks={borrowedBooks} />
    </>
  )
}

export default App
