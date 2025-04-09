import { useEffect } from "react";
import authService from "../services/authService";
import { useState } from "react";

function Admin() {
    const [users, setUsers] = useState([]);
      const [currentUser, setCurrentUser] = useState(authService.getCurrentUser());
    
    
    useEffect(()=>{
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
        const response = await authService.getAllUsers();
        setUsers(response.data.filter(user => user.id !== Number(currentUser.userId)));
        } catch (error) {
        console.error('Error fetching users', error);
        }
    };

  return (
    <div className="admin">
      <h1>Admin</h1>
      <p>This is the admin page.</p>
      <div className="users">
        <ul>
        {users && users.map(user => {
            return (
                <li key={user.id}>
                    <p>{user.userName}</p>
                    <p>{user.email}</p>
                    <p>{user.age}</p>
                    <p>{user.phoneNumber}</p>
                    <button onClick={() => {
                        authService.deleteUser(user.id).then(() => {
                            setUsers(users.filter(u => u.id !== user.id));
                            alert("User deleted successfully");
                        }).catch(err => {
                            console.error("Error deleting user", err);
                            alert("Error deleting user");
                        });
                    }}>
                    Delete user
                    </button>
                </li>
            )
            
        })}
        </ul>
      </div>
    </div>
  );
}

export default Admin;