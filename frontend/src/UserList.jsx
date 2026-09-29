import {useState, useEffect} from "react";
import AddUserForm from "./AddUserForm.jsx";

function UserList({ user }) {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionError, setActionError] = useState('');

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

    const handleCreated = (created) => {
        setUsers((prev) => [...prev, created]);
    };

    const handleDelete = async (target) => {
        if (!window.confirm(`Hapus user "${target.username}"?`)) return;

        setActionError('');
        try {
            const response = await fetch(`http://localhost:8080/api/users/${target.id}`,{
                method: 'DELETE',
                headers: { Authorization: `Bearer ${user.token}` },
            });

            if (!response.ok) {
                const errorText = await response.text();
                setActionError(errorText || 'Gagal menghapus user');
                return;
            }

            setUsers((prev) => prev.filter((u) => u.id !== target.id));
        } catch (err) {
            setActionError('Tidak bisa terhubung ke server');
        }
    };

    if (loading) return <p>Memuat data user...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <h2>Data User</h2>
            <AddUserForm token={user.token} onCreated={handleCreated}/>

            {actionError && <p>{actionError}</p>}

            <table border="1" cellPadding="8">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Role</th>
                    <th>Aksi</th>
                </tr>
                </thead>
                <tbody>
                {users.map((u) => (
                    <tr key={u.id}>
                        <td>{u.id}</td>
                        <td>{u.username}</td>
                        <td>{u.role}</td>
                        <td>
                            {u.username === user.username ? (
                                <em>(sedang login)</em>
                            ) : (
                                <button onClick={() => handleDelete(u)}>Hapus</button>
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default UserList;

// Yang baru di sini:
// Tiga kondisi tampilan: loading, error, dan sukses. Ada state error baru, dan dua baris if (...) return di atas menentukan apa yang tampil.
// Kenapa 403 nggak di-redirect ke /login seperti di Layout: user staff itu sudah login dengan benar, dia cuma nggak berhak. Melempar dia ke halaman login justru membingungkan, jadi kita tampilkan pesan.
// .map() + key untuk baris tabel, pola yang sama seperti menu. Variabel barisnya u, bukan user, supaya nggak bentrok dengan prop user (yang login).
// cellPadding ditulis camelCase karena aturan JSX. border dan cellPadding ini styling sementara sampai tahap styling nanti.

//Konsep React yang baru di sini:
//
// <select> terkontrol: pola sama seperti <input> (value + onChange). Di React, value dipasang di <select>, bukan selected di <option>.
// try / catch / finally: finally selalu jalan, termasuk saat ada return di dalam try. Ini menjamin tombol tidak nyangkut di "Menyimpan...", dan disabled={submitting} mencegah klik ganda.
// Update state tanpa mengubah yang lama (immutability): [...prev, created] dan prev.filter(...) menghasilkan array baru. React mendeteksi perubahan dari referensi baru, jadi prev.push(...) tidak akan memicu render ulang. Bentuk (prev) => ... memakai nilai state paling mutakhir.
// Anak memberi kabar ke induk lewat props fungsi (onCreated): sama seperti onLoginSuccess di Login. AddUserForm tidak perlu tahu isi tabel.
// Tombol Hapus disembunyikan untuk akun sendiri: itu cuma kemudahan UI. Backend tetap menolak, prinsip yang sama dengan menyembunyikan menu.