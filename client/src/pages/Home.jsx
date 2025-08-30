import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from "../context/AuthContext"; 

const words = ['Interviews', 'DSA'];

function Home() {
  const navigate = useNavigate();
  const { authUser } = useAuth();
  const spansRef = useRef([]);

  useEffect(() => {
    let index = 0;

    const animate = () => {
      spansRef.current.forEach((el) => {
        el.classList.remove('text-in');
      });

      spansRef.current[index].classList.add('text-in');

      index = (index + 1) % words.length;
    };

    animate(); // initial
    const interval = setInterval(animate, 3000); // same as animation duration

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="h-full bg-black text-white flex items-center justify-center">
      <main className="text-center px-4 relative">
        <h2 className="text-4xl sm:text-6xl font-extrabold mb-6">Welcome!</h2>

        <div className="text-3xl sm:text-4xl text-gray-300 mb-12 max-w-2xl mx-auto">
          A place to practice, improve, and master{' '}
          <span className="relative h-[2.5rem] w-[150px] text-yellow-400 block">
            <span className="absolute left-60 top-3 w-full text-center animate-text">
              {words.map((word, i) => (
                <div
                  key={i}
                  ref={(el) => (spansRef.current[i] = el)}
                  className=""
                >
                  {word}
                </div>
              ))}
            </span>
          </span>
        </div>
        <button
            onClick={() => {
              if (authUser) {
                navigate('/problems');
              } else {
                navigate('/login');
              }
            }}
            className="text-white border border-white px-6 py-3 rounded cursor-pointer 
            hover:bg-white hover:text-black transition text-base sm:text-lg"
            >
            Practice: Problems →
    </button>
      </main>
    </div>
  );
}

export default Home;
