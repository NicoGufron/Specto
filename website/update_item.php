<?php 
require_once("connect.php");
session_start();
include("navbar.html");
if($_GET['id'] && $_GET['subject']){
    if($_GET['subject'] == 'video'){
        $sql = "SELECT * from ".$_GET['subject']."s where id = ".$_GET['id'];
        $q = mysqli_query($conn,$sql);
    }else if($_GET['subject'] == 'photo'){
        $sql = "SELECT * from ".$_GET['subject']."s where id = ".$_GET['id'];
        $q = mysqli_query($conn,$sql);
    }
}else{
    header('location:login.php');
}
?>
<!doctype html>
<html>
    <head>
        <link rel='stylesheet' href='forodal.css'>
        <style>
            .container{
                padding-top:3%;
            }
            h1,p{
                padding-top:3%;
                text-align:center;
            }
        </style>
    </head>
    <body>
    <h1>Editing Page</h1>
        <p>Here, you can replace your video or fill information that match your videos.</p>
        <div class="container">
            <div class="table-responsive">
                    <?php
                    // buat video
                        if($_GET['subject']=='video'){
                            while($datavideo = mysqli_fetch_assoc($q)){
                                $table = "<table class='table table-hover'>
                                            <tr class='table-light'>
                                                <th>Thumbnail</th>
                                                <th>Video Name</th>
                                                <th>Date Taken</th>
                                                <th>Start Location</th>
                                                <th>Location End</th>
                                            </tr>";
                                echo $table;
                                $thumbnails = $datavideo['thumbnail'];
                                $videoname = $datavideo["video_name"];
                                $startloca = $datavideo["start_location"];
                                $endloca = $datavideo["end_location"];
                                $date_taken = $datavideo["date_taken"];
                                echo "<tr>";
                                echo "<form method='post' action='' enctype='multipart/form-data'>";
                                echo "<th>
                                        <video width='300' height='200' controls>
                                            <source src='uploads/videos/$thumbnails' type='video/mp4'>
                                        </video>
                                        <input type='file' class='form-control-file' name='videofile'>
                                    </th>";
                                echo "<th><input class='form-control' type='text' placeholder='$videoname' name='videoname'></th>";
                                echo "<th><input type='date' class='form-control' placeholder='$date_taken'</th>";
                                echo "<th><input type='text' class='form-control' placeholder='$startloca'></th>";
                                echo "<th><input type='text' class='form-control' placeholder='$endloca'></th>";
                                echo "</form>";
                                echo "</tr>";
                            }
                        }else if($_GET['subject'] == 'photo'){
                            // buat photo
                            while($dataphoto = mysqli_fetch_assoc($q)){

                                $thumbnailp = $dataphoto['thumbnail'];
                                $description = $dataphoto['description'];
                                $locationp = $dataphoto['location'];
                                $datetakenp = $dataphoto['date_taken'];

                                $table = 
                                    "<table class='table table-hover'>
                                        <tr class='table-light'>
                                            <th>Thumbnail</th>
                                            <th>Photo Location</th>
                                            <th>Description</th>
                                            <th>Photo Taken</th>
                                        </tr>";
                                echo $table;
                                echo "<tr>";
                                echo "<th>
                                    <img alt='$description' src='uploads/photos/$thumbnailp' class='img-thumbnail' width='200' height='200'>
                                        <div id='modalImg' class='modal'>
                                            <span class='close'>&times</span>
                                            <img class='modal-content' id='imgm'>
                                            <div id='caption'></div>
                                        </div>
                                        <input type='file' class='form-control-file' name='photofile'>
                                    </th>";
                                echo "<th><input type='text' value='$locationp' class = 'form-control'></th>";
                                echo "<th><input type='text' value='$description' class = 'form-control'></th>";
                                echo "<th><input type='date' value='$datetakenp'></th>";
                                echo "</tr>";
                            }
                        }
                    ?>
                </table>
            </div>
        </div>
    </body>
    <script>
        var modal = document.getElementById('modalImg');

        var img = document.getElementsByClassName('img-thumbnail');
        var i = img.length;
        var modalImg = document.getElementById('imgm');
        var caption = document.getElementById('caption');
        for(var j = 0; j < i; j++){
            img[j].onclick = function(){
                modal.style.display='block';
                modalImg.src = this.src;
                caption.innerHTML = this.alt;
            }
        }
        var span = document.getElementsByClassName('close');

        for(var j = 0 ; j < span.length;j++){
            span[j].onclick = function(){
                modal.style.display='none';
            }
}

    </script>
</html>