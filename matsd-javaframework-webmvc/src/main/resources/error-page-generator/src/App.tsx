import { useState } from 'react'
import ErrorPage from './components/ErrorPage'
import { mockErrorData } from './data/mockErrorData'

const App = () => {
  const [errorData] = useState(mockErrorData)

  return (
    <ErrorPage errorData={errorData} />
  )
};

export default App;
