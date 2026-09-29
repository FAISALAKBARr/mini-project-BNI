import {useState} from "react";

function AddRoleForm({ token, onCreated }) {
    const [name, setName] = useState('');
    const [message, setMassage] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMassage('');
        setSubmitting(true);

        try {
            const response = await fetch('http://localhost:8080/api/roles', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ name }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                setMassage(errorText || 'Gagal menambah role');
                return;
            }

            const created = await response.json();
            onCreated(created);
            setName('');
        } catch (error) {
            setMassage('Tidak bisa terhubung ke server');
        } finally {
            setSubmitting(false);
        }
    };

    return(
        <form onSubmit={handleSubmit}>
            <h3>Tambah Role</h3>
            <input
                type="text"
                placeholder="Nama role (contoh: SUPERVISOR)"
                value={name}
                onChange={(e) => setName(e.target.value)}
            />
            <button type="submit" disabled={submitting}>
                {submitting ? 'Menyimpan...' : 'Tambah'}
            </button>
            {message && <p>{message}</p>}
        </form>
    );
}

export default AddRoleForm;