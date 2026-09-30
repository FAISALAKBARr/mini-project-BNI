import {useEffect, useState} from "react";
import {Label} from "@/components/ui/label.jsx";
import {Input} from "@/components/ui/input.jsx";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select.jsx";
import {Button} from "@/components/ui/button.jsx";

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
        <form onSubmit={handleSubmit} className="flex flex-col gap-4 sm:flex-row sm:flex-wrap sm:items-end">
            <div className="flex flex-col gap-2">
                <Label htmlFor="new-username">Username</Label>
                <Input
                    id="new-username"
                    type="text"
                    autoComplete="off"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
            </div>
            <div className="flex flex-col gap-2">
                <Label htmlFor="new-password">Password</Label>
                <Input
                    id="new-password"
                    type="password"
                    autoComplete="new-password"
                    placeholder="Min. 6 karakter"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
            </div>
            <div className="flex flex-col gap-2">
                <Label>Role</Label>
                <Select value={role} onValueChange={setRole}>
                    <SelectTrigger className="w-40">
                        <SelectValue placeholder="Pilih role" />
                    </SelectTrigger>
                    <SelectContent>
                        {roles.map((r) => (
                            <SelectItem key={r.id} value={r.name}>{r.name}</SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>
            <Button type="submit" disabled={submitting || roles.length === 0}>
                {submitting ? 'Menyimpan...' : 'Tambah'}
            </Button>
            {message && <p className="text-sm text-destructive basis-full">{message}</p>}
        </form>
    );
}

export default AddUserForm;

//Dua atribut autoComplete dipasang supaya browser tidak mengisi otomatis kredensial tersimpan (admin/admin123) ke form ini.
//Kalau masih terisi sendiri, itu perilaku browser, bukan bug kode.
// disabled={submitting || roles.length === 0} mencegah submit sebelum daftar role selesai dimuat.