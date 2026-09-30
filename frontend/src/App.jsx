import { useState } from 'react'
import {Routes, Route, Navigate, data} from "react-router-dom";
import Layout from "./Layout.jsx";
import Login from "./Login.jsx";
import Dashboard from "./Dashboard.jsx";
import UserList from "./UserList.jsx";
import RoleList from "./RoleList.jsx";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.jsx";

// function Greeting({ name }){
//   return <h2>Halo, {name}</h2>
// }
//
// function Behavior({perlakuan}){
//   return <h1>Mari {perlakuan}</h1>
// }

// Halaman sementara, nanti diganti halaman asli satu per satu
function Placeholder({ title }) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>{title}</CardTitle>
            </CardHeader>
            <CardContent>
                <p className="text-sm text-muted-foreground">Halaman ini masih kosong, nanti diisi.</p>
            </CardContent>
        </Card>
    );
}

function App(){
    const [user, setUser] = useState(()=>{
        const stored = localStorage.getItem('user');
        return stored ? JSON.parse(stored) : null;
    });

    const handleLoginSuccess = (data) => {
        localStorage.setItem('user', JSON.stringify(data));
        setUser(data);
    };

    const handleLogout = () => {
        localStorage.removeItem('user');
        setUser(null);
    };

  return(
      <Routes>
          <Route path="/login" element={<Login onLoginSuccess={handleLoginSuccess} />} />

          {/* Layout route: nggak punya path, tugasnya membungkus route-route di dalamnya */}
          <Route element={user ? <Layout user={user} onLogout={handleLogout} /> : <Navigate to="/login" />}>
              <Route path="/dashboard" element={<Dashboard user={user} />} />
              <Route path="/master/user" element={<UserList user={user} />} />
              <Route path="/master/role" element={<RoleList user={user} />} />
              <Route path="/transaksi" element={<Placeholder title="Transaksi" />} />
          </Route>

          <Route path="*" element={<Navigate to={user ? "/dashboard" : "/login"} />} />
      </Routes>
    // <div>
    //     {user ? <Dashboard user={user}/> : <Login onLoginSuccess={setUser}/>}
    // </div>
  );
}
export default App;

// useEffect — useEffect(callback, [dependencies]). Callback-nya jalan otomatis setelah component muncul di layar.
// Array di belakang nentuin kapan jalan lagi:
// [] (kosong) -> cuma sekali, pas pertama muncul
// [user.roleId] -> sekali pas muncul, DAN jalan ulang kalau nilai user.roleId berubah di antara render
// Aturan praktisnya: masukkan semua nilai dari luar yang dipakai di dalam callback,
// di sini user.roleId dipakai di URL fetch, jadi wajib masuk dependency array.
// Ini beda banget sama Login kemarin: fetch di Login jalan karena event (klik submit), fetch di sini jalan otomatis karena effect (component muncul).

// Component -> Greeting
// { name } -> di param fungsi ini adalah cara nerima Props. "Langsung buka object props dan ambil field name-nya"
// <Greeting name="Budi" /> -> cara pake component, dimana kirim prop "name".
// { name } di dalam <h2> -> JSX nyisip nilai variabel ke dlm tampilan.
// App -> Component. Dia yg manggil Greeting dua kali pake "Prop" yang beda, itu reusability nya component.
