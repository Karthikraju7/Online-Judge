import React from 'react'

const Footer = () => {
  return (
    <footer className="bg-gray-900 text-white text-center py-4 text-sm">
        © {new Date().getFullYear()} CodeTheCode. All rights reserved.
    </footer>
  )
}

export default Footer