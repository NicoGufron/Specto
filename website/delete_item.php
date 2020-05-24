<?php
require_once("connect.php");
session_start();
if(isset($_SESSION['email'])){
    if($_GET['id'] && $_GET['subject']){
        if($_GET['subject'] == "video"){
            $sql = "DELETE FROM videos where id = ". $_GET['id'];
            $q = mysqli_query($conn,$sql);
            if($q == false){
                echo '<script>alert("Delete failed!")</script>';
            }else{
                header('location:storage.php');
            }
        }else if($_GET['subject'] == "photo"){
            $sql = "DELETE FROM photos where id = ". $_GET['id'];
            $q = mysqli_query($conn,$sql);
            if($q == false){
                echo '<script>alert("Delete failed!")</script>';
            }else{
                header('location:storage.php');
            }
        }else{
            echo '<script>alert("Delete failed! No subject is specified!")</script>';
        }
    }else{
        echo '<script>alert("Delete failed! ID and subject not found!")</script>';
    }
}else{
    header('location:login.php');
}

?>