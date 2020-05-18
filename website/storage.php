<?php 
require_once("connect.php");

?>
<!DOCTYPE html>
<html>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="bootstrap/bootstrap.css">
    <style>
        /* Side nav only */
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
    /* Side Nav only */
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
        <a href="account.php" class="active">Account</a>
        <a href="storage.php">Storage</a>
        <a href="#">About</a>
    </div>
    <div class="main">
        <?php 
        $id = 1;
        $video_name = ""?>
    </div>
    </body>
</html>