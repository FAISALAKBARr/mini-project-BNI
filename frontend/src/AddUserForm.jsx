import {useState} from "react";

function AddUserForm({ token, onCreated}) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('STAFF');
    const [message, setMessage] = useState('');
    const [submitting, setSubmitting] = useState(false);

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
            setRole('STAFF');
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
                <option value="STAFF">STAFF</option>
                <option value="ADMIN">ADMIN</option>
            </select>
            <button type="submit" disabled={submitting}>
                {submitting ? 'Menyimpan...' : 'Tambah'}
            </button>
            {message && <p>{message}</p>}
        </form>
    );
}

export default AddUserForm;

//Dua atribut autoComplete dipasang supaya browser tidak mengisi otomatis kredensial tersimpan (admin/admin123) ke form ini.
//Kalau masih terisi sendiri, itu perilaku browser, bukan bug kode.