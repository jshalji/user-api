const express = require("express");
const cors = require("cors");

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const users = [
    {
        LastName: "Ocio",
        FirstName: "Josh Alji E.",
        Email: "josh@example.com",
        Password: "sample123"
    },
    {
        LastName: "Santos",
        FirstName: "Mark",
        Email: "mark@example.com",
        Password: "sample456"
    },
    {
        LastName: "Reyes",
        FirstName: "Anna",
        Email: "anna@example.com",
        Password: "sample789"
    }
];

app.get("/", (req, res) => {
    res.json(users);
});

app.post("/login", (req, res) => {
    const { Email, Password } = req.body;

    if (!Email || !Password) {
        return res.status(400).json({
            success: false,
            message: "Email and password are required."
        });
    }

    const user = users.find(
        user =>
            user.Email === Email &&
            user.Password === Password
    );

    if (user) {
        return res.status(200).json({
            success: true,
            message: "Login successful!",
            user: {
                LastName: user.LastName,
                FirstName: user.FirstName,
                Email: user.Email
            }
        });
    }

    return res.status(401).json({
        success: false,
        message: "Invalid email or password."
    });
});

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});