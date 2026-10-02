import {useEffect, useState} from "react";
import {
    Dialog,
    DialogContent,
    DialogDescription, DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog.jsx";
import {Button} from "@/components/ui/button.jsx";
import {Label} from "@/components/ui/label.jsx";
import {Input} from "@/components/ui/input.jsx";

function EditRoleDialog({role, token, onUpdated}) {
    const [open, setOpen] = useState(false);
    const [name, setName] = useState(role.name);
    const [message, setMessage] = useState('');
    const [submitting, setSubmitting] = useState(false);

    // Reset isian tiap dialog dibuka, biar nggak kebawa sisa input lama yang batal disimpan
    useEffect(() => {
        if (open) {
            setName(role.name);
            setMessage('');
        }
    }, [open, role.name]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setSubmitting(true);

        try {
            const response = await fetch(`http://localhost:8080/api/roles/${role.id}`,{
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({name}),
            });

            if (!response.ok){
                const errorText = await response.text();
                setMessage(errorText || 'Gagal mengubah role');
                return;
            }

            const updated= await response.json();
            onUpdated(updated);
            setOpen(false);

        }catch (error){
            setMessage('Tidak bisa terhubung ke server');
        }finally {
            setSubmitting(false);
        }
    };

    return(
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger render={<Button variant="outline" size="sm">Edit</Button>}/>
            <DialogContent>
                <form onSubmit={handleSubmit}>
                    <DialogHeader>
                        <DialogTitle>Edit Role</DialogTitle>
                        <DialogDescription>Ubah nama role "{role.name}".</DialogDescription>
                    </DialogHeader>
                    <div className="flex flex-col gap-2 py-4">
                        <Label htmlFor={`edit-role-${role.id}`}>Nama Role</Label>
                        <Input
                            id={`edit-role-${role.id}`}
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                        />
                        {message && <p className="text-sm text-destructive">{message}</p>}
                    </div>
                    <DialogFooter>
                        <Button type="submit" disabled={submitting}>
                            {submitting ? 'Menyimpan...' : 'Simpan'}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
} export default EditRoleDialog;