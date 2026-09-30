import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

function AddRoleForm({ token, onCreated }) {
    const [name, setName] = useState('');
    const [message, setMessage] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
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
                setMessage(errorText || 'Gagal menambah role');
                return;
            }

            const created = await response.json();
            onCreated(created);
            setName('');
        } catch (error) {
            setMessage('Tidak bisa terhubung ke server');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4 sm:flex-row sm:items-end">
            <div className="flex flex-1 flex-col gap-2">
                <Label htmlFor="new-role-name">Nama Role</Label>
                <Input
                    id="new-role-name"
                    type="text"
                    placeholder="Contoh: SUPERVISOR"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
            </div>
            <Button type="submit" disabled={submitting}>
                {submitting ? 'Menyimpan...' : 'Tambah'}
            </Button>
            {message && <p className="text-sm text-destructive basis-full">{message}</p>}
        </form>
    );
}

export default AddRoleForm;