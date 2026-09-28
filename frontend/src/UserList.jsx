import { useState, useEffect } from "react";

function UserList({ user }) {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetch('http://localhost:8080/api/users', {
            headers: {
                Authorization: `Bearer ${user.token}`,
            },
        })
            .then((res) => {
                if (res.status === 403) {
                    throw new Error('Kamu tidak punya akses ke halaman ini (atau sesi login sudah berakhir).');
                }
                if (!res.ok) {
                    throw new Error('Gagal memuat data user.');
                }
                return res.json();
            })
            .then((data) => {
                setUsers(data);
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, [user.token]);

    if (loading) return <p>Memuat data user...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <h2>Data User</h2>
            <table border="1" cellPadding="8">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Role</th>
                </tr>
                </thead>
                <tbody>
                {users.map((u) => (
                    <tr key={u.id}>
                        <td>{u.id}</td>
                        <td>{u.username}</td>
                        <td>{u.role}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default UserList;

// Yang baru di sini:
//
// Tiga kondisi tampilan: loading, error, dan sukses. Ada state error baru, dan dua baris if (...) return di atas menentukan apa yang tampil.
// Kenapa 403 nggak di-redirect ke /login seperti di Layout: user staff itu sudah login dengan benar, dia cuma nggak berhak. Melempar dia ke halaman login justru membingungkan, jadi kita tampilkan pesan.
// .map() + key untuk baris tabel, pola yang sama seperti menu. Variabel barisnya u, bukan user, supaya nggak bentrok dengan prop user (yang login).
// cellPadding ditulis camelCase karena aturan JSX. border dan cellPadding ini styling sementara sampai tahap styling nanti.
