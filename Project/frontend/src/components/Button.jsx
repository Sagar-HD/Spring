import React from 'react'

const Button = ({ children, className, type, ...props }) => {
  return (
      <button
              className={`bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline ${className}`}
              type={type}
              {...props}

            >
                {children}
            </button>
  )
}

export default Button