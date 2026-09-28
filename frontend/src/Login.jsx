import {useState} from "react";
import {useNavigate} from "react-router-dom";

function Login({onLoginSuccess}) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault(); // cegah form reload halaman (default behavior HTML)

        try {
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username, password}),
            });

            if (!response.ok){
                const errorText = await response.text();
                setMessage(`Login gagal: ${errorText}`);
                return;
            }

            const data = await response.json();
            onLoginSuccess(data);
            navigate('/dashboard');
        } catch (error) {
            setMessage('Tidak bisa terhubung ke server');
        }
    };
    return (
        <div>
            <h2>Login</h2>
            <form onSubmit={handleLogin}>
                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <button type="submit">Login</button>
            </form>
            {message && <p>{message}</p>}
        </div>
    );
}

export default Login;

//State —> data milik sebuah component yang bisa berubah, dan tiap berubah, React otomatis re-render tampilannya.
//useState —> Hook buat "mendaftarkan" state di component. Polanya: const [value, setValue] = useState(nilaiAwal). value itu nilai sekarang, setValue fungsi buat mengubahnya.
//Event —> cara React nangkep aksi user (ketik, klik, submit). Ditulis camelCase: onChange, onClick, onSubmit.

//value={username} => nilai yang ditampilkan input selalu ngikutin state username
//onChange={(e) => setUsername(e.target.value)} => tiap ketik satu huruf, event ini nyala; e.target.value adalah teks di input saat itu, setUsername update state-nya
//State berubah => React re-render -> tampilan ikut update. Siklus inilah yang bikin <p> di bawah selalu sinkron sama apa yang kamu ketik

//Ini pola yang disebut controlled input — dan username, password yang tersimpan di state ini persis yang bakal kita kirim ke API login Spring Boot