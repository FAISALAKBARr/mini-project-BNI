import {useState} from "react";
import {useNavigate} from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Card,
    CardHeader,
    CardTitle,
    CardDescription,
    CardContent,
} from "@/components/ui/card";

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
        <div className="flex min-h-svh items-center justify-center bg-muted p-4">
            <Card className="w-full max-w-sm">
                <CardHeader>
                    <CardTitle className="text-2xl">Login</CardTitle>
                    <CardDescription>Mini Project BNI</CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleLogin} className="flex flex-col gap-4">
                        <div className="flex flex-col gap-2">
                            <Label htmlFor="username">Username</Label>
                            <Input
                                id="username"
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                            />
                        </div>
                        <div className="flex flex-col gap-2">
                            <Label htmlFor="password">Password</Label>
                            <Input
                                id="password"
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </div>
                        <Button type="submit" className="w-full">Login</Button>
                        {message && (
                            <p className="text-sm text-destructive text-center">{message}</p>
                        )}
                    </form>
                </CardContent>
            </Card>
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

//Logic-nya (state, handleLogin, fetch) sama persis kayak sebelumnya — yang berubah cuma JSX-nya, dibungkus komponen shadcn.