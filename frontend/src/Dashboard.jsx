import {Card, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.jsx";

function Dashboard({ user }) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>Dashboard</CardTitle>
                <CardDescription>
                    Selamat datang, {user.username} ({user.role})
                </CardDescription>
            </CardHeader>
        </Card>
    );
}

export default Dashboard;