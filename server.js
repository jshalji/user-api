const express = require("express");

const app = express();
const PORT = process.env.PORT || 3000;

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

app.get("/users", (req, res) => {
  res.json(users);
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running on port ${PORT}`);
});
