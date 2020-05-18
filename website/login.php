<?php
require_once("connect.php");
session_start();
if(isset($_SESSION["email"])){
    header("storage.php");
}
if($_POST){
    $email = $_POST["email"];
    $pass = $_POST["password"];
    $_SESSION = $email;

    // query untuk ngecek email ama pass
    // $sql = "SELECT * from users where email = '$email' and password = '$pass'";
    // $q = mysqli_query($conn, $sql);

    // $flag = mysqli_num_rows($q);
    $flag = 0;
    if($flag == 1){
        $result = 
            "<div class='alert alert-dismissible alert-success'>
                <button type='button' class='close' data-dismiss='alert'>&times;</button>
                <strong>Logged in successfully!</strong>
            </div>";
            header("Refresh: 3, url= storage.php");
    }else{
        $result =
        "<div class='alert alert-dismissible alert-danger'>
            <button type='button' class='close' data-dismiss='alert'>&times;</button>
            <strong>Wrong email or password!</strong>
      </div>";  
    }
}
?>
<!DOCTYPE html>
<html>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <head>    
    <link rel="stylesheet" href="bootstrap/bootstrap.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
        <style>
            .main{
                margin: 0 auto;
                padding-top:3%;
                background-color:#FFFFFF;
                border-style: solid;
                border-color: black;
                margin-left:20%;
                margin-right:20%;
                padding-left:8%;
                padding-right:8%;
                padding-bottom:9%;
            }
            h1{
                text-align:center;
                padding-top:4%;
            }
            .form-control{
                margin-bottom:15px;
                width:100%;
            }
            button{
                width:100%;
            }
        </style>
    </head>
    <body>
        <!-- nav bar -->
        <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
            <a class="navbar-brand" href="home.html">Specto</a>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarColor01" aria-controls="navbarColor01" aria-expanded="false" aria-label="Toggle navigation"></button>
        </nav>

        <h1>LOGIN</h1>
        <div class="main">
        <?php echo $result; ?>

            <form action= "" method="post" class="form-group">
                <input class="form-control" type="email" placeholder="Your email" name="email">
                <input class="form-control" type="password" placeholder="Your password" name="password">
                <label>
                    <input type="checkbox" checked="checked" name="remember"> Remember me
                </label>
                <br>
                <button class="btn btn-primary">Login</button>
            </form>
        </div>
    </body>
</html>