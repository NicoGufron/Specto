<?php
require_once("connect.php");
session_start();
include ("navbar.html");
$result = "";
if(isset($_SESSION['email'])){
    $email = $_SESSION['email'];
    $sql = "SELECT name,password FROM users where email = '$email'";
    $q = mysqli_query($conn,$sql);
    $data = mysqli_fetch_assoc($q);
    $name = $data['name'];
    $password = $data['password'];
}else{
    header("location: login.php");
}
if($_POST){
    $name = $_POST["name"];
    $email = $_POST["email"];
    $pass1 = $_POST["pass1"];
    $pass2 = $_POST["pass2"];
    if($pass1 != $pass2){
        $result = 
            "<div class='alert alert-dismissible alert-danger'>
                <button type='button' class='close' data-dismiss='alert'>&times;</button>
                <strong>Passwords are not the same!</strong>
            </div>";
    }else{
        $sql = "UPDATE users SET name = '$name', email = '$email', password = '$pass1'";
        $q = mysqli_query($conn,$sql);
        if($q == TRUE){
            $result = 
               "<div class='alert alert-dismissible alert-success'>
                   <button type='button' class='close' data-dismiss='alert'>&times;</button>
                   <strong>Updates saved!</strong>
               </div>";
        }else{
            $result = 
            "<div class='alert alert-dismissible alert-danger'>
                <button type='button' class='close' data-dismiss='alert'>&times;</button>
                <strong>Oops! Something went wrong!</strong>
            </div>";
        }
    }
}
?>
<!DOCTYPE HTML>

<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="bootstrap/bootstrap.css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    </head>
    <style>
    .sidenav{
        height: 100%;
        width: 0;
        position:fixed;
        z-index:1;
        top: 0;
        left: 0;
        background-color: #E95420;
        overflow-x: hidden;
        padding-top:60px;
        transition: 0.5s;
    }
    .sidenav a{
        padding: 8px 8px 8px 32px;
        text-decoration: none;
        font-size: 25px;
        color: #FFFFFF;
        display: block;
        transition: 0.3s;
    }
    .sidenav a:hover{
        color: black;
    }
    .sidenav .closebtn {
        position: absolute;
        top: 0;
        right: 25px;
        font-size:36px;
        margin-left:50px;
    }
    .main{
        margin: 0 auto;
        padding: 3%;
    }
    #upper{
        padding-left:3%;
        padding-top:3%;
    }
    </style>
    <body>
    <div id="upper">
        <h1>Welcome, <?php echo $name?></h1>
        <p>Manage your info and privacy.</p>
    </div>
    <?php echo $result; ?>
    <div class ="main">
        
        <form method="post" action="">
            Name:<br>
                <input type="text" placeholder="Your name" class="form-control" value= <?php echo $name?> name="name"><br>
            Email:<br>
                <input type="email" placeholder="Your email" class="form-control"value=<?php echo $email?> name="email"><br>
            Password: <br>
                <input type="password" class="form-control" placeholder="Your password" name="pass1"><br>
            Confirm password:
            <input type="password" class="form-control" placeholder="Enter your new password" name="pass2">
            <br>
            <button class="btn btn-primary">Submit</button>
        </form>
    </div>
    </body>
    <script>
    function openNav(){
        document.getElementById("sidenav").style.width="250px";
    }
    function closeNav(){
        document.getElementById("sidenav").style.width = "0px";
    }
</script>
</html>