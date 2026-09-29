import {useEffect, useState} from "react";
import {data} from "react-router-dom";
import AddRoleForm from "./AddRoleForm.jsx";

function RoleList({ user }) {
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionError, setActionError] = useState('');

    useEffect(() => {
        fetch('http://localhost:8080/api/roles', {
            headers: {Authorization: `Bearer ${user.token}`},
        })
            .then((res) => {
                if (!res.ok) throw new Error('Gagal memuat data role.');
                return res.json();
            })
            .then((data) => {
                setRoles(data);
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, [user.token]);

    const handleCreated = (created) => {
        setRoles((prev) => [...prev, created]);
    };

    const handleDelete = async (target) => {
        if (!window.confirm(`Hapus role "${target.name}"?`)) return;

        setActionError('');
        try{
            const response = await fetch(`http://localhost:8080/api/roles/${target.id}`, {
                method: 'DELETE',
                headers: { Authorization: `Bearer ${user.token}`},
            });

            if (!response.ok) {
                const errorText = await response.text();
                setActionError(errorText || 'Gagal menghapus role');
                return;
            }

            setRoles((prev) => prev.filter((r) => r.id !== target.id));
        } catch (err) {
            setActionError('Tidak bisa terhubung ke server');
        }
    };

    if (loading) return <p>Memuat data role...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <h2>Data Role</h2>

            <AddRoleForm token={user.token} onCreated={handleCreated} />

            {actionError && <p>{actionError}</p>}

            <table border="1" cellPadding="8">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nama Role</th>
                        <th>Aksi</th>
                    </tr>
                </thead>
                <tbody>
                {roles.map((r) => (
                    <tr key={r.id}>
                        <td>{r.id}</td>
                        <td>{r.name}</td>
                        <td>
                            <button onClick={() => handleDelete(r)}>Hapus</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default RoleList;