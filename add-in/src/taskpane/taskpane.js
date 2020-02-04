/*
    Copyright (c) 2020 Obeo. 
       All rights reserved. This program and the accompanying materials
       are made available under the terms of the Eclipse Public License v1.0
       which accompanies this distribution, and is available at
       http://www.eclipse.org/legal/epl-v10.html
        
        Contributors:
            Obeo - initial API and implementation
*/

/* global document, Office, Word */

window.onload = function () {
  document.getElementById("startButton").onclick = startApp;
};

Office.onReady(info => {
  if (info.host === Office.HostType.Word) {
    startApp();
  }
});

export function startApp() {
  document.getElementById("sideload-msg").style.display = "none";
  document.getElementById("app-body").style.display = "flex";
  var expressionInput = document.getElementById("expression");
  window.awesomplete = new Awesomplete(expressionInput, {
    minChars: 0, 
    autoFirst: true,
    maxItems: 20,
    filter: function(text, input) {
      return true;
    },
    sort: false,
    replace: applyReplacement
  });
  expressionInput.oninput = openProposals;
  expressionInput.addEventListener('awesomplete-close', function (event) {
    if (event.reason == "esc") {
      // TODO validate the input that will be applied
      validationClear();
      // TODO evaluate the input that will be applied
      resultClear();
    }
  }, false);
  var lastIndex;
  expressionInput.addEventListener('awesomplete-highlight', function (event) {
    if ((window.awesomplete.index != window.awesomplete.maxItems) && (window.awesomplete.index != window.awesomplete._list.length) && (window.awesomplete.index >= 0)) {
      var documentation = document.getElementById("documentation");
      if (documentation == null) {
        documentation = document.createElement("li");
        documentation.id = "documentation";
        documentation.style.backgroundColor = "lightblue";
        document.getElementById("awesomplete_list_1_item_0").parentElement.appendChild(documentation);
      }
      documentation.innerHTML = window.awesomplete._list[window.awesomplete.index].documentation;
      lastIndex = window.awesomplete.index;
    } else {
      window.awesomplete.goto(Math.min(window.awesomplete.maxItems, window.awesomplete._list.length) - lastIndex - 1);
    }
  }, false);

  window.onresize = function(event) {
    document.getElementById("genconfURI").style.width = (window.innerWidth - 20) + "px";
    document.getElementById("expression").style.width = (window.innerWidth - 20) + "px";
  }
  document.getElementById("genconfURI").style.width = (window.innerWidth - 20) + "px";
  document.getElementById("expression").style.width = (window.innerWidth - 20) + "px";

  validationClear();
  resultClear();
}

export function openProposals() {
  var genconfURIInput = document.getElementById("genconfURI");
  var expressionInput = document.getElementById("expression");
  var expression = expressionInput.value;
  var genconfURI = genconfURIInput.value;
  var offset = expressionInput.selectionStart;

  validate(expression);
  evaluate(expression);
  var ajax = new XMLHttpRequest();
  ajax.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        window.awesomplete.list = JSON.parse(this.responseText);
        window.awesomplete.open();
      } else if (this.status == 400) {
        validationError(this.responseText);
      }
    }
  };
  ajax.open("GET", "/rest?command=proposal&genconfURI=" + encodeURI(genconfURI) + "&expression=" + encodeURI(expression) + "&offset=" + offset, true);
  ajax.send();
}

export function applyReplacement(text) {
  var genconfURIInput = document.getElementById("genconfURI");
  var expressionInput = document.getElementById("expression");
  var expression = expressionInput.value;
  var genconfURI = genconfURIInput.value;
  var offset = expressionInput.selectionStart;

  var ajax = new XMLHttpRequest();
  ajax.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        window.awesomplete.input.value = this.responseText;
        validate(this.responseText);
        evaluate(this.responseText);
      } else if (this.status == 400) {
        validationError(this.responseText);
      }
    }
  };
  ajax.open("GET", "/rest?command=apply&genconfURI=" + encodeURI(genconfURI) + "&expression=" + encodeURI(expression) + "&offset=" + offset +"&completion=" + encodeURI(text), true);
  ajax.send();
}

export function validate(expression) {
  var genconfURIInput = document.getElementById("genconfURI");
  var genconfURI = genconfURIInput.value;

  var ajax = new XMLHttpRequest();
  ajax.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        var messages = JSON.parse(this.responseText);
        if (messages && messages.length) {
          validationMessages(messages);
        } else {
          validationClear();
        }
      } else {
        validationClear();
      }
    } else if (this.status == 400) {
      validationError(this.responseText);
    }
  };
  ajax.open("GET", "/rest?command=validate&genconfURI=" + encodeURI(genconfURI) + "&expression=" + encodeURI(expression), true);
  ajax.send();
}

export function evaluate(expression) {
  var genconfURIInput = document.getElementById("genconfURI");
  var genconfURI = genconfURIInput.value;

  var ajax = new XMLHttpRequest();
  ajax.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        showResult(this.responseText);
      } else {
        resultClear();
      }
    } else if (this.status == 400) {
      validationError(this.responseText);
    }
  };
  ajax.open("GET", "/rest?command=evaluate&genconfURI=" + encodeURI(genconfURI) + "&expression=" + encodeURI(expression), true);
  ajax.send();
}

export function validationMessages(messages) {
  var html = "";
  for (var i = 0; i < messages.length; i++) {
    var message = messages[i];
    var color;
    if (message.level == "ERROR") {
      color = "lightcoral";
    } else if (message.level == "WARNING") {
      color = "lightgoldenrodyellow";
    } else if (message.level == "INFO") {
      color = "lightblue";
    } else {
      color = "";
    }
    html += "<p style=\"background-color:" + color + "\"><img src=\"/assets/" + message.level + ".png\">" + message.message + " (" + message.start + ", " + message.end + ")</p>";
  }
  document.getElementById("validationDiv").innerHTML = html;
}

export function validationError(message) {
  var validationDiv = document.getElementById("validationDiv");
  validationDiv.style.backgroundColor = "lightcoral";
  validationDiv.innerHTML = "<p>" + message + "</p>";
}

export function validationWarning(message) {
  var validationDiv = document.getElementById("validationDiv");
  validationDiv.style.backgroundColor = "lightgoldenrodyellow";
  validationDiv.innerHTML = "<p>" + message + "</p>";
}

export function validationClear() {
  var validationDiv = document.getElementById("validationDiv");
  validationDiv.style.backgroundColor = "";
  validationDiv.innerHTML = "";
}

export function showResult(html) {
  var resultDiv = document.getElementById("resultDiv");
  resultDiv.style.backgroundColor = "lightblue";
  resultDiv.innerHTML = html;
}

export function resultClear() {
  showResult("");
}
