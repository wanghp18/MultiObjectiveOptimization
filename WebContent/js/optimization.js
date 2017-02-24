
	var defaultData = [['Objective 1', 'Objective 2']];
	var globaldata = defaultData;
	var globalTime = 0.0;
	
	google.charts.load('current',{'packages':['corechart','table']});
	//google.charts.load('current', {'packages':['table']});
	//google.charts.setOnLoadCallback(drawChart);
	
	function drawDisplay(){
		var problem=$('#problem_select').val();
		var algorithm=$('#algorithm_select').val();
		var data = google.visualization.arrayToDataTable(globaldata);
		var view = new google.visualization.DataView(data);
		view.setColumns([0, 1]);
		
		var options = {
				title: "Problem: "+problem+" Algorithm: "+algorithm+" Results, Elapse Time: "+globalTime+"ms",
				hAxis: {title:'Objective 1', minValue: 0.0, maxValue: 1.0},
				vAxis: {title: 'Objective 2', minValue: 0.0, maxValue: 1.0},
				legend: 'none'
		};
	    var chart = new google.visualization.ScatterChart(document.getElementById('chart_div'));
	    google.visualization.events.addListener(chart,'select',selectHandler);
	    chart.draw(data, options);
	    
		var table = new google.visualization.Table(document.getElementById('table_div'));
		
		google.visualization.events.addListener(table, 'sort', function(event){
			data.sort([{column: event.column, desc: !event.ascending}]);
			chart.draw(view, options);
		});
		table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});
	}
	
	function selectHandler(e){
		console.log(e);
		alert('A dot was selected');
		
	}
	
	function calculateMOP(){
		
		var problem = $('#problem_select').val();
		var algorithm = $('#algorithm_select').val();
		$.ajax({
			url: './RequestHandler?problem=' + problem + '&algorithm=' + algorithm,
			success: function(data){
				//clear array
				globaldata.length = 0;
				globaldata = [['Objective 1', 'Objective 2']];
				
				//console.log(data);
				var obj1Array = data["Obj1"];
				var obj2Array = data["Obj2"];
				var timeArray = data["Time"];

				globalTime = timeArray["Time"];
				
				var index = 0;
				while(true){
					if(!obj1Array[index+""]){
						break;
					}
					var arr = [];
					arr.push(obj1Array[index+""]);
					arr.push(obj2Array[index+""]);
					globaldata.push(arr);
					index++;
				}
			},
			
			dataType: 'json',
			async: false
		});
		//console.log(globaldata);
		drawDisplay();
	}


