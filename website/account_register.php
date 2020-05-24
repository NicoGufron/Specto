<?php 
require_once("connect.php");
session_start();
$result = "";
if(isset($_POST['email'])){
    $name = $_POST['fname'];
    $email = $_POST['email'];
    $pass1 = $_POST['password1'];
    $pass2 = $_POST['password2'];
    $_SESSION = $_POST['email'];
    $sql = "SELECT email from users where email = '$email';";
    $q = mysqli_query($conn,$sql);
    if(mysqli_num_rows($q) >= 1){
        $result = 
        "<div class='alert alert-dismissible alert-danger'>
            <button type='button' class='close' data-dismiss='alert'>&times;</button>
            <strong>Another user is using the email!</strong>
        </div>";
    }else{
        if($pass1 == $pass2){
            $sql = "INSERT INTO users (name,email,password) values ('$name','$email','$pass1')";
            $q = mysqli_query($conn,$sql);
            if($q == true){
                    $result = 
                    "<div class='alert alert-dismissible alert-success'>
                        <button type='button' class='close' data-dismiss='alert'>&times;</button>
                        <strong>Registered successfully! Redirecting you to login</strong>
                    </div>";
                    header("Refresh: 3, url= storage.php");
            }else{
                $result = 
                "<div class='alert alert-dismissible alert-danger'>
                    <button type='button' class='close' data-dismiss='alert'>&times;</button>
                    <strong>Oops! Something went wrong</strong>
                </div>";
            }
        }else{
            $result= 
                "<div class='alert alert-dismissible alert-danger'>
                    <button type='button' class='close' data-dismiss='alert'>&times;</button>
                    <strong>Passwords does not match!</strong>
                </div>";
        }
    }
}
?>
<!DOCTYPE HTML>
<html>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="bootstrap/bootstrap.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    <head>
        <style>
            h1,p{
                text-align:center;
                padding-top:3%;
            }
            h4{
                text-align:center;
                margin-bottom:30px;
            }
            .main{
                margin: 0 auto;
                padding-top:3%;
                background-color:#FFFFFF;
                border-style: solid;
                border-color: black;
                margin-left:15%;
                margin-right:15%;
                padding-left:8%;
                padding-right:8%;
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
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <a class="navbar-brand" href="home.html">Specto</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarColor01" aria-controls="navbarColor01" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
      </nav>
      <div class="container">
          <?php echo $result ?>
            <h1>Welcome to Specto!</h1>
                    <div class="main">
                    <h4>Let's get you on board!</h4>
                        <form action= "" method="post" class="form-group">
                            <input class="form-control" type="text" placeholder="Full Name" name="fname">
                            <input class="form-control" type="email" placeholder="Your email" name="email">
                            <input class="form-control" type="password" placeholder="Your password" name="password1">
                            <input class="form-control" type="password" placeholder="Confirm password" name="password2"></label><br>
                            <button class="btn btn-primary">Register</button>
                        </form>
                        <p>Already have an account? <a href="login.php"><u>Sign In</u></a></p>
                    </div>
                </div>
        </div>
    </body>
    <script>
    
    </script>
</html>