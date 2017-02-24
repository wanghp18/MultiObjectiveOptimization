var optimizeData = [
    ["Operation Cost","First Cost"],
    [2324763,2286433],
    [1945480,1309218],
    [1808939,2114405],
    [2160938,1260953],
    [2256787,1287545],
    [1810763,2181651],
  ]

var designs = [
    ["Gener","Wall","Roof","Window","Light","Daylight","HVAC"],
    ["Baseline","Concrete Block Walls","Metal Deck Roof","Double Clear","T8","Off","VAV"],
    ["Optimized 1","EIFS", "Sloped Frame Roof","Triple Glazing","LED","On","VRF"],
    ["Optimized 2","Precast and CIP Wall","Concrete Roof","Triple Glazing","LED","On","VRF"],
    ["Optimized 3","Stud Walls","Wood Deck","Heat Reflective Clear","LED","Off","VRF"],
    ["Optimized 4","Concrete Block Walls","Concrete Roof","Triple Glazing","T5","On","VRF"],
    ["Optimized 5","EIFS","Sloped Frame Roof","Triple Glazing","LED","On","VRF"]
    ]

var testData = [
	["Operation Cost","First Cost","Wall","Roof","Window","Light","Daylight","HVAC"],   
	[2324763,2286433,"Concrete Block Walls","Metal Deck Roof","Double Clear","T8","Off","VAV"],
	[1945480,1309218,"EIFS", "Sloped Frame Roof","Triple Glazing","LED","On","VRF"],
	[1808939,2114405,"Precast and CIP Wall","Concrete Roof","Triple Glazing","LED","On","VRF"],
	[2160938,1260953,"Stud Walls","Wood Deck","Heat Reflective Clear","LED","Off","VRF"],
	[2256787,1287545,"Concrete Block Walls","Concrete Roof","Triple Glazing","T5","Off","VRF"],
	[1810763,2181651,"EIFS","Sloped Frame Roof","Triple Glazing","LED","On","VRF"]
	]

var energyData =[
    ["Heating","Cooling","Lighting","Equipment","Fans","Pumps","Operation Cost","First Cost"],
    [1020514.78,352058.48,86255.97,88972.35,207145.03,156.11,2324763,2286433],
    [121752.58,51691.21,79768.99,88972.35,245225.29,0,1945480,1309218],
    [122282.04,50358.19,58653.67,88972.35,244500.89,0,1808939,2114405],
    [655941.23,262688.06,103507.17,88972.35,147052.43,0,2160938,1260953],
    [146341.36,58721.53,103507.17,88972.35,296580.23,0,2256787,1287545],
    [145804.50,48772.78,103507.17,88972.35,274695.57,0,1810763,2181651]
    ]

var firstCostData=[
    ["Wall","Roof","Window","Light","Daylight","HVAC"],
    [319135.26,61690.44,147050.09,84058.71,0,1674498.5],
    [91643.06,245733.41,291440.79,39360.96,17420,623619.78],
    [394842.59,267197.61,291440.79,370456.07,17420,773047.94],
    [152138.25,31430.51,291440.79,39360.96,0,746582.5],
    [53309.81,39668.30,216649.80,16794.92,0,961122.17],
    [100157.82,37261.51,147050.09,370456.07,17420,1509305.5]
    ]

var firstCostPieData=[
    ["Genre","Base","Optimized 1","Optimized 2","Optimized 3","Optimized 4","Optimized 5"],
    ["Wall",319135.26,91643.06,394842.59,152138.25,53309.81,100157.82],
    ["Roof",61690.44,245733.41,267197.61,31430.51,39668.30,37261.51],
    ["Window",147050.09,291440.79,291440.79,291440.79,216649.80,147050.09],
    ["Light",84058.71,39360.96,370456.07,39360.96,16794.92,370456.07],
    ["Daylight",0,17420,17420,0,0,17420],
    ["HVAC",1674498.5,623619.78,773047.94,746582.5,961122.17,1509305.5]
    ]

var operationCostPieData=[
	["Genre","Base","Optimized 1","Optimized 2","Optimized 3","Optimized 4","Optimized 5"],
	["Energy",2223472.08,1849919.13,1777859.29,2051998.97,2077450.44,2173299.04],
	["Maintenance",47553.00,5102.60,4226.63,4226.63,4226.63,4226.63],
	["Replacement",53737.61,68017.58,28677.46,70518.07,79260.89,79260.89]
    ]

var operationCostData=[
    ["Energy","Maintenance","Replacement"],
    [2223472.08,47553,53737.61],
    [1849919.13,5102.60,68017.58],
    [1777859.29,4226.63,28677.46],
    [2051998.97,4226.63,70518.07],
    [2077450.44,4226.63,79260.89],
    [2173299.04,4226.63,79260.89]
    ]

Morris.Bar({
    element: 'morris-bar-chart',
    data: [{
    	genre: 'Base',
        heating: 1020514.78,
        cooling: 352058.48,
        lighting: 86255.97,
        equipment: 88972.35,
        fans: 207145.03,
        pumps: 156.11,
    }, {
    	genre: 'Optimized1',
        heating: 121752.58,
        cooling: 51691.21,
        lighting: 79768.99,
        equipment: 88972.35,
        fans: 245225.29,
        pumps: 0.0,
    }, {
    	genre: 'Optimized2',
        heating: 122282.04,
        cooling: 50358.19,
        lighting: 58653.67,
        equipment: 88972.35,
        fans: 244500.89,
        pumps: 0.0,
    }, {
    	genre: 'Optimized3',
        heating: 128083.81,
        cooling: 54288.70,
        lighting: 58653.67,
        equipment: 88972.35,
        fans: 260260.33,
        pumps: 0.0,
    },{
    	genre: 'Optimized4',
        heating: 146341.36,
        cooling: 58721.53,
        lighting: 103507.17,
        equipment: 88972.35,
        fans: 296580.23,
        pumps: 0.0,
    }, {
    	genre: 'Optimized5',
        heating: 145804.50,
        cooling: 48772.78,
        lighting: 103507.17,
        equipment: 88972.35,
        fans: 274695.57,
        pumps: 0.0,
    }],
    xkey: 'genre',
    ykeys: ['heating', 'cooling','lighting','equipment','fans','pumps'],
    labels: ['Heating (kWh)', 'Cooling (kWh)',"Lighting (kWh)","Equipment (kWh)","Fan (kWh)","Pumps (kWh)"],
    hideHover: 'auto',
    resize: true,
    stacked: true
});


