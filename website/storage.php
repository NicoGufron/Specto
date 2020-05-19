<?php 
require_once("connect.php");
session_start();
include ("navbar.html");
$result = "";
if(isset($_SESSION['email'])){
    // tampilin semua query dari foto dan video
    $sqlv = "SELECT * FROM videos";
    $sqlp = "SELECT * from photos";
    $qv = mysqli_query($conn,$sqlv);
    $qp = mysqli_query($conn,$sqlp);

    // $datavideo = mysqli_fetch_assoc($qv);
    $dataphoto = mysqli_fetch_assoc($qp);

    // data video sek
    while($datavideo = mysqli_fetch_assoc($qv)){
        $thumbnail = 
    }
    
    // buat upload video
    if(isset($_POST['hiddentag']) == 1){

        $target_dir = "uploads/videos/";
        $target_file = $target_dir.basename($_FILES["videofile"]["name"]);
        $fileType = pathinfo($target_file,PATHINFO_EXTENSION);
        var_dump($target_file);
        if($fileType != "mp4" && $fileType != "avi" && $fileType != "mov" && $fileType != "mpeg"){
            $result = 
            "<div class='alert alert-dismissible alert-danger'>
                <button type='button' class='close' data-dismiss='alert'>&times;</button>
                Upload only <strong>mp4, avi, mov</strong> and <strong> mpeg </strong> !
            </div>";
        }else{
            $videoPath = $_FILES['videofile']['name'];
            $video_name = $_POST['video_name'];
            $startloc = $_POST['start_location'];
            $endloc = $_POST['end_location'];
            $datetaken = $_POST['date_taken'];
            // $sql = "INSERT INTO videos (thumbnail,video_name,date_taken,start_location,end_location) VALUES ('$videoPath','$video_name','$datetaken','$startloc','$endloc');";
            // $q = mysqli_query($conn,$sql);
            move_uploaded_file($_FILES['videofile']['tmp_name'],$target_file);
            if($q == TRUE){
                $result = 
                    "<div class='alert alert-dismissible alert-success'>
                        <button type='button' class='close' data-dismiss='alert'>&times;</button>
                        <strong>Uploaded successfully!</strong>
                    </div>";
            }else{
                $result = 
                "<div class='alert alert-dismissible alert-danger'>
                    <button type='button' class='close' data-dismiss='alert'>&times;</button>
                    <strong>Upload failed!</strong>
                </div>";
            }
        }
    }else{
        echo "hehe :D";
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
            .container{
                padding-top:3%;
            }
        </style>
    </head>
    <body>
        <?php echo $result; ?>
        <div class="container">
        <h1>Storage</h1>
            <ul class="nav nav-tabs">
                <li class="nav-item"><a class = "nav-link active" href="#videos" data-toggle="tab">Videos</a></li>
                <li class="nav-item"><a class = "nav-link" href="#photos" data-toggle="tab">Photos</a></li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="videos">
            <!-- nah ini isi konten -->
                    <div class="table-responsive">
                        <table class="table table-hover">   
                            <tr class="table-light">    
                                <th>Thumbnail</th>  
                                <th>Video Name</th> 
                                <th>Date Taken</th> 
                                <th>Start Location</th> 
                                <th>Location End</th>   
                            </tr>   
                            <!-- value table -->  
                            <tr>    
                                <th></th>   
                                <th>asd.mp3</th>    
                                <th>19/5/2020</th>  
                                <th>Jl.A.Yani</th>  
                                <th>Jl. Panjaitan</th>  
                            </tr>
                        </table>
                    </div>
                    <div class="form-group">
                        <form method="post" action="" enctype="multipart/form-data">
                            <br><br><hr>
                            <label>Upload a new video file</label>
                                <input type="text" name="video_name" class="form-control" placeholder="Video name"><br>
                                <input type="text" name="start_location" class="form-control" placeholder="Start Location"><br>
                                <input type="text" name="end_location" class="form-control" placeholder="Location End"><br>
                                <input type="hidden" name="hiddentag" value="1"> 
                                <label>Date taken: </label><br>
                                <input type="date" name="date_taken" class="form-control-date"><br><br>
                                <input type="file" class="form-control-file" name="videofile"><br>
                                <input type="submit" value="Upload" class="btn btn-primary">
                        </form>
                    </div>
                </div>
                <div class="tab-pane" id="photos">
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <tr class="table-light">
                                <th>Thumbnail</th>
                                <th>Photo Location</th>
                                <th>Description</th>
                                <th>Photo taken</th>
                            </tr>
                            <!-- value table -->
                            <tr>
                                <th></th>
                                <th>Jl. Panjaitan</th>
                                <th>Something went wrong here.</th>
                                <th>5/19/2020</th>
                        </table>
                    </div>
                        <form>
                            <label>Upload new photo files</label>
                                <input type="hidden" name="hiddentag" value="2"> 
                                <input type="file" class="form-control-file" name="photofile" >
                        </form>
                </div>
            </div>
            <!-- end tab pane -->
        </div>
    </body>
</html>