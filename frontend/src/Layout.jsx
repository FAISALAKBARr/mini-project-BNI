import { useState, useEffect } from "react";
import { NavLink, Outlet, useNavigate, useLocation } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
    SidebarProvider,
    Sidebar,
    SidebarHeader,
    SidebarContent,
    SidebarFooter,
    SidebarSeparator,
    SidebarGroup,
    SidebarGroupLabel,
    SidebarGroupContent,
    SidebarMenu,
    SidebarMenuItem,
    SidebarMenuButton,
    SidebarInset,
    SidebarTrigger,
} from "@/components/ui/sidebar";
import {LoaderIcon} from "lucide-react";
import {cn} from "cn";

const activeMenuClass = "data-active:bg-primary data-active:text-primary-foreground";

function Spinner({ className, ...props }) {
    return (
        <LoaderIcon
            role="status"
            aria-label="Loading"
            className={cn("size-4 animate-spin", className)}
            {...props}
        />
    );
}

function Layout({ user, onLogout }) {
    const [menus, setMenus] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        fetch('http://localhost:8080/api/menus/me', {
            headers: { Authorization: `Bearer ${user.token}` },
        })
            .then((res) => {
                if (!res.ok) throw new Error('Token tidak valid atau kadaluarsa');
                return res.json();
            })
            .then((data) => {
                setMenus(data);
                setLoading(false);
            })
            .catch(() => {
                navigate('/login');
            });
    }, [user.token, navigate]);

    if (loading) {
        return (
            <div className="flex min-h-svh items-center justify-center">
                <Spinner/>
            </div>
        );
    }

    const topLevelMenus = menus.filter((m) => m.parentId === null);
    const getSubMenus = (parentId) => menus.filter((m) => m.parentId === parentId);
    const noSubMenus = topLevelMenus.filter((m) => getSubMenus(m.id).length === 0);
    const groupedMenus = topLevelMenus.filter((m) => getSubMenus(m.id).length > 0);

    return (
        <SidebarProvider>
            <Sidebar>
                <SidebarHeader className="p-4">
                    <div className="flex items-center gap-2">
                        <img src="/bank-bni.png" alt="Logo BNI" className="h-8 w-8 object-contain" />
                        <h2 className="font-semibold">Mini Project BNI</h2>
                    </div>
                    <div className="mt-2 flex items-center gap-2">
                        <span className="text-sm">{user.username}</span>
                        <Badge variant="secondary">{user.role}</Badge>
                    </div>
                </SidebarHeader>

                <SidebarSeparator />

                <SidebarContent>
                    <SidebarGroup>
                        <SidebarGroupContent>
                            <SidebarMenu>
                                <SidebarMenuItem>
                                    <SidebarMenuButton
                                        render={<NavLink to="/dashboard" />}
                                        isActive={location.pathname === "/dashboard"}
                                        className={activeMenuClass}
                                    >
                                        Dashboard
                                    </SidebarMenuButton>
                                </SidebarMenuItem>
                                {noSubMenus.map((menu) => (
                                    <SidebarMenuItem key={menu.id}>
                                        <SidebarMenuButton
                                            render={<NavLink to={menu.path} />}
                                            isActive={location.pathname === menu.path}
                                            className={activeMenuClass}
                                        >
                                            {menu.name}
                                        </SidebarMenuButton>
                                    </SidebarMenuItem>
                                ))}
                            </SidebarMenu>
                        </SidebarGroupContent>
                    </SidebarGroup>

                    {groupedMenus.map((menu) => (
                        <SidebarGroup key={menu.id}>
                            <SidebarGroupLabel>{menu.name}</SidebarGroupLabel>
                            <SidebarGroupContent>
                                <SidebarMenu>
                                    {getSubMenus(menu.id).map((sub) => (
                                        <SidebarMenuItem key={sub.id}>
                                            <SidebarMenuButton
                                                render={<NavLink to={sub.path} />}
                                                isActive={location.pathname === sub.path}
                                                className={activeMenuClass}
                                            >
                                                {sub.name}
                                            </SidebarMenuButton>
                                        </SidebarMenuItem>
                                    ))}
                                </SidebarMenu>
                            </SidebarGroupContent>
                        </SidebarGroup>
                    ))}
                </SidebarContent>

                <SidebarSeparator />

                <SidebarFooter className="p-4">
                    <Button variant="outline" onClick={onLogout}>
                        Logout
                    </Button>
                </SidebarFooter>
            </Sidebar>

            <SidebarInset>
                <header className="flex items-center gap-2 border-b p-4">
                    <SidebarTrigger />
                </header>
                <main className="flex-1 p-6">
                    <Outlet />
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}

export default Layout;