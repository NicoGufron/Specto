<?php
if($_SESSION){
    $email = "nicogufron@gmail.com";
    $name = "Nico Gufron";
}

?>
<!DOCTYPE HTML>

<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="bootstrap/bootstrap.css">
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
        <!-- navbar -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <a class="navbar-brand" href="#" onclick="openNav()">Specto</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarColor01" aria-controls="navbarColor01" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
      </nav>

      <!-- sidemenu -->
    <div id ="sidenav" class="sidenav">
        <a href="javascript:void(0)" class="closebtn" onclick="closeNav()">&times;</a>
        <a href="#" class="active">Account</a>
        <a href="storage.php">Storage</a>
        <a href="#">About</a>
    </div>
    <div id="upper">
        <h1>Welcome, <?php echo $name?></h1>
        <p>Manage your info and privacy.</p>
    </div>
    <div class ="main">
        
        <form method="post" action="account_change.php">
            Name:<br>
                <input type="text" placeholder="Your name" class="form-control" value= <?php echo $name?> name="name"><br>
            Email:<br>
                <input type="email" placeholder="Your email" class="form-control"value=<?php echo $email?> name="email"><br>
            Password: <br>
                <input type="password" class="form-control" placeholder="Your password" name="pass"><br>
            Confirm password:
            <input type="password" class="form-control" placeholder="Enter your new password" name="ppass">
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