import {useEffect, useState} from "react";
import {data} from "react-router-dom";

function AddUserForm({ token, onCreated}) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('');
    const [roles, setRoles] = useState([]);
    const [message, setMessage] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetch('http://localhost:8080/api/roles', {
            headers: { Authorization: `Bearer ${token}`},
        })
            .then((res) => res.json())
            .then((data) => {
                setRoles(data);
                if (data.length > 0) setRole(data[0].name);
            });
    }, [token]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setSubmitting(true);

        try {
            const response = await fetch('http://localhost:8080/api/users',{
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ username, password, role }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                setMessage(errorText || 'Gagal menambah user');
                return;
            }

            const created = await response.json();
            onCreated(created); //unutk mengabari parent (UserList) supaya tabel diperbrui
            setUsername('');
            setPassword('');
        } catch (error) {
            setMessage('Tidak bisa terhubung ke server');
        } finally {
            setSubmitting(false);
        }
    };

    return(
        <form onSubmit={handleSubmit}>
            <h3>Tambah User</h3>
            <input
                type="text"
                placeholder="Username"
                autoComplete="off"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />
            <input
                type="password"
                placeholder="Password (min. 6 karakter)"
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />
            <select value={role} onChange={(e) => setRole(e.target.value)}>
                {roles.map((r) => (
                    <option key={r.id} value={r.name}>{r.name}</option>
                ))}
            </select>
            <button type="submit" disabled={submitting || roles.length === 0}>
                {submitting ? 'Menyimpan...' : 'Tambah'}
            </button>
            {message && <p>{message}</p>}
        </form>
    );
}

export default AddUserForm;

//Dua atribut autoComplete dipasang supaya browser tidak mengisi otomatis kredensial tersimpan (admin/admin123) ke form ini.
//Kalau masih terisi sendiri, itu perilaku browser, bukan bug kode.
// disabled={submitting || roles.length === 0} mencegah submit sebelum daftar role selesai dimuat.