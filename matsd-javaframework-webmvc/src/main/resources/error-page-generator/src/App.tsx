import { useState } from 'react'
import './App.css'
import ErrorPage from './components/ErrorPage'
import { mockErrorData } from './data/mockErrorData'

function App() {
  const [errorData] = useState(mockErrorData)

  return (
    <ErrorPage errorData={errorData} />
  )
}

export default App
