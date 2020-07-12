require: birds.csv
    var = $Birds
    name = Birds
    
patterns:
    $Stop = (хватит|перестань|прекрати|закончим|сдаюсь|стоп|пока)
    $Help = (помоги*|помочь|помощь|не знаю)

theme: /
    state: start
        q!: * *start 
        script:
            $session.all_birds = []
            for (var i = 0; i < 3; i++) {
                $session.all_birds.push($Birds[i].value.name);
            }
        a: Привет. Со мной ты можешь научиться определять птиц по голосу.

        state: song
            q!: *
            a: {{$session.all_birds[0]}}
                
            audio: https://drive.google.com/file/d/1KtXXC_RBpwB27xC9K_6L3pU0TQ1ee96A/view?usp=sharing
            
            state: right
                q: * соловей *
                a: Верно, это {{$session.next_bird.name}}. 
                script:
                    $session.score=$session.score + 1;
                    $reactions.answer("Угадано уже '{{$session.score}}' птиц. Молодец!");
                go: /song
    
            state: Help
                q: * $Help *
                a: Это {{$session.next_bird.name}}. 
    
            
            state: wrong
                q: *
                a: Неверно
                    
            state: Stop
                q: $Stop
                a: Было приятно сыграть.
                go!: /reset
        
            state: reset
                q!: (reset|* *start)
                script:
                    $session = {}
                    $client = {}
                    $temp = {}
                    $response = {}
                go!: /